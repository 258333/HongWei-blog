package com.hongwei.interceptor;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hongwei.constants.RabbitConst;
import com.hongwei.constants.RedisConst;
import com.hongwei.domain.email.CommentEmail;
import com.hongwei.domain.email.LeaveWordEmail;
import com.hongwei.domain.email.ReplyCommentEmail;
import com.hongwei.domain.entity.User;
import com.hongwei.enums.MailboxAlertsEnum;
import com.hongwei.mapper.UserMapper;
import com.hongwei.utils.RedisCache;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author: HongWei
 * @date: 2024/11/16 11:06
 * @description: 邮件队列监听器
 **/
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailQueueListener {

    private final JavaMailSender mailSender;

    private final TemplateEngine templateEngine;

    private final RedisCache redisCache;
    
    private final EmailInformMapper emailInformMapper;
    
    private final CommentMapper commentMapper;
    
    private final UserMapper userMapper;
    
    private final LeaveWordMapper leaveWordMapper;

    @Value("${spring.mail.username}")
    private final String username;

    @Value("${mail.link.apply.redirect-uri}")
    private final String linkApplyRedirectUri;

    @Value("${web.index.path}")
    private final String webIndexPath;


    /**
     * 监听邮件队列
     * @Description:  * handlerMapMessage 方法通过 @RabbitListener 注解监听特定的邮件队列
     * (RabbitConst.MAIL_QUEUE)。当队列中有消息时，该方法会被触发。
     *
     * 根据消息的 type 字段判断邮件类型。
     * 解析消息数据，组装邮件内容。
     * 使用不同的模板发送 HTML 邮件（调用 sendHtmlMail 方法）。
     */
    @RabbitListener(queues = RabbitConst.MAIL_QUEUE,
            errorHandler = "rabbitListenerErrorHandler",
            containerFactory = "rabbitListenerContainerFactory")
    public void handlerMapMessage(Map<String, Object> data) {
        String email = (String) data.get("email");
        String code = (String) data.get("code");
        String type = (String) data.get("type");

        String encode = "";

        if (type.equals(MailboxAlertsEnum.FRIEND_LINK_APPLICATION.getCodeStr())) {
            // 生成本次会话token
            encode = String.valueOf(System.currentTimeMillis());
            // 存入redis，七天有效
            redisCache.setCacheObject(RedisConst.EMAIL_VERIFICATION_LINK + encode, data.get("linkId") + " " + data.get("linkEmail"), 7, TimeUnit.DAYS);
        }

        CommentEmail commentEmail = null;

        // 用户评论文章通知站长
        if (type.equals(MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getCodeStr())) {
            // 准备数据
            commentEmail = emailInformMapper.getCommentEmailOne(String.valueOf(data.get("commentId")), (Integer) data.get("commentType"));
            String url = Objects.equals(commentEmail.getType(), 1) ? webIndexPath + "article/" + commentEmail.getTypeId() : webIndexPath + "message/detail/" + commentEmail.getTypeId();
            commentEmail.setUrl(url);
            commentEmail.setType(commentEmail.getType());
            // 如果是留言 title就为null
            if (Objects.isNull(commentEmail.getTitle())) commentEmail.setTitle("");
        }

        ReplyCommentEmail replyCommentEmail = null;

        // 用户回复，通知给回复人
        if (type.equals(MailboxAlertsEnum.REPLY_COMMENT_NOTIFICATION_EMAIL.getCodeStr())) {
            // 回复评论数据
            CommentEmail commentEmailOne = emailInformMapper.getCommentEmailOne(String.valueOf(data.get("commentId")), (Integer) data.get("commentType"));
            // 被回复评论数据
            Comment replyComment = commentMapper.selectOne(new LambdaQueryWrapper<Comment>().eq(Comment::getId, data.get("replyCommentId")));
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, replyComment.getCommentUserId()));
            replyCommentEmail = commentEmailOne.asViewObject(ReplyCommentEmail.class, reply -> {
                reply.setReplyAvatar(user.getAvatar());
                reply.setReplyNickname(user.getNickname());
                reply.setReplyContent(replyComment.getCommentContent());
                reply.setReplyTime(TimeUtils.format(replyComment.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
                String url = Objects.equals(commentEmailOne.getType(), 1) ? webIndexPath + "article/" + commentEmailOne.getTypeId() : webIndexPath + "message/detail/" + commentEmailOne.getTypeId();
                reply.setUrl(url);
            });
            // 如果是留言 title就为null
            if (Objects.isNull(replyCommentEmail.getTitle())) replyCommentEmail.setTitle("");
            log.info("信息:{}", replyCommentEmail);
        }

        // 新的留言提醒
        LeaveWordEmail leaveWordEmail = null;
        if (type.equals(MailboxAlertsEnum.MESSAGE_NOTIFICATION_EMAIL.getCodeStr())) {
            LeaveWord messageUser = leaveWordMapper.selectOne(new LambdaQueryWrapper<LeaveWord>().eq(LeaveWord::getId, data.get("messageId")));
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, messageUser.getUserId()));
            leaveWordEmail = messageUser.asViewObject(LeaveWordEmail.class, message -> {
                message.setUrl(webIndexPath + "message/detail/" + messageUser.getId());
                message.setAvatar(user.getAvatar());
                message.setNickname(user.getNickname());
                message.setTime(TimeUtils.format(messageUser.getCreateTime(), DateUtils.YYYY_MM_DD_HH_MM_SS));
            });
        }

        MimeMessage mimeMessage;

        if (MailboxAlertsEnum.REGISTER.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.REGISTER.getSubject(), MailboxAlertsEnum.REGISTER.getTemplateName(), Map.of(
                    "expirationTime", "5分钟",
                    "code", code,
                    "toUrl", webIndexPath,
                    "openSourceAddress", "https://github.com/258333/HongWei-blog"
            ));
        } else if (MailboxAlertsEnum.RESET.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.RESET.getSubject(), MailboxAlertsEnum.RESET.getTemplateName(), Map.of(
                    "expirationTime", "5分钟",
                    "code", code,
                    "toUrl", webIndexPath,
                    "openSourceAddress", "https://github.com/258333/HongWei-blog"
            ));
        } else if (MailboxAlertsEnum.RESET_EMAIL.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.RESET_EMAIL.getSubject(), MailboxAlertsEnum.RESET_EMAIL.getTemplateName(), Map.of(
                    "expirationTime", "5分钟",
                    "code", code,
                    "toUrl", webIndexPath,
                    "openSourceAddress", "https://github.com/258333/HongWei-blog"
            ));
        } else if (MailboxAlertsEnum.FRIEND_LINK_APPLICATION.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.FRIEND_LINK_APPLICATION.getSubject(), MailboxAlertsEnum.FRIEND_LINK_APPLICATION.getTemplateName(), Map.of(
                    "name", data.get("name"),
                    "url", data.get("url"),
                    "description", data.get("description"),
                    "background", data.get("background"),
                    "linkEmail", data.get("linkEmail"),
                    "toUrl", webIndexPath,
                    "verifyCode", linkApplyRedirectUri + "?verifyCode=" + encode
            ));
        } else if (MailboxAlertsEnum.FRIEND_LINK_APPLICATION_PASS.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.FRIEND_LINK_APPLICATION_PASS.getSubject(), MailboxAlertsEnum.FRIEND_LINK_APPLICATION_PASS.getTemplateName(), Map.of(
                    "toUrl", webIndexPath + "link",
                    "openSourceAddress", "https://github.com/258333/HongWei-blog"
            ));
        } else if (MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getSubject(), MailboxAlertsEnum.COMMENT_NOTIFICATION_EMAIL.getTemplateName(), Map.of(
                    "toUrl", commentEmail.getUrl(),
                    "type", commentEmail.getType(),
                    "title", commentEmail.getTitle(),
                    "url", commentEmail.getUrl(),
                    "avatar", commentEmail.getAvatar(),
                    "nickname", commentEmail.getNickname(),
                    "content", commentEmail.getContent(),
                    "time", commentEmail.getTime()
            ));
        } else if (MailboxAlertsEnum.REPLY_COMMENT_NOTIFICATION_EMAIL.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.REPLY_COMMENT_NOTIFICATION_EMAIL.getSubject(), MailboxAlertsEnum.REPLY_COMMENT_NOTIFICATION_EMAIL.getTemplateName(), toReplyMap(replyCommentEmail));
        }else if (MailboxAlertsEnum.MESSAGE_NOTIFICATION_EMAIL.getCodeStr().equals(type)) {
            mimeMessage = sendHtmlMail(email, MailboxAlertsEnum.MESSAGE_NOTIFICATION_EMAIL.getSubject(), MailboxAlertsEnum.MESSAGE_NOTIFICATION_EMAIL.getTemplateName(), Map.of(
                    "toUrl", leaveWordEmail.getUrl(),
                    "avatar", leaveWordEmail.getAvatar(),
                    "nickname", leaveWordEmail.getNickname(),
                    "content", leaveWordEmail.getContent(),
                    "time", leaveWordEmail.getTime()
            ));
        } else mimeMessage = null;

        if (Objects.isNull(mimeMessage)) return;
        // 发送邮件
        mailSender.send(mimeMessage);
        log.info("{}邮件发送成功", email);
    }

    /**
     * 构建回复评论提示邮箱数据
     *
     * @param replyCommentEmail 回复对象
     * @return Map
     */
    private Map<String, Object> toReplyMap(ReplyCommentEmail replyCommentEmail) {
        return Map.ofEntries(
                Map.entry("toUrl", replyCommentEmail.getUrl()),
                Map.entry("type", replyCommentEmail.getType()),
                Map.entry("title", replyCommentEmail.getTitle()),
                Map.entry("url", replyCommentEmail.getUrl()),
                Map.entry("avatar", replyCommentEmail.getAvatar()),
                Map.entry("nickname", replyCommentEmail.getNickname()),
                Map.entry("content", replyCommentEmail.getContent()),
                Map.entry("time", replyCommentEmail.getTime()),
                Map.entry("replyAvatar", replyCommentEmail.getReplyAvatar()),
                Map.entry("replyNickname", replyCommentEmail.getReplyNickname()),
                Map.entry("replyContent", replyCommentEmail.getReplyContent()),
                Map.entry("replyTime", replyCommentEmail.getReplyTime())
        );
    }


    /**
     * 发送普通邮件
     *
     * @param title   主题
     * @param content 内容
     * @param email   收件人
     */
    private SimpleMailMessage createMessage(String title, String content, String email) {
        // 创建一个SimpleMailMessage对象
        SimpleMailMessage message = new SimpleMailMessage();

        // 设置邮件主题
        message.setSubject(title);

        // 设置邮件内容
        message.setText(content);

        // 设置收件人
        message.setTo(email);

        // 设置发件人
        message.setFrom(username);

        return message;
    }

    /**
     * 发送htm模板邮件
     *
     * @param toEmail      发送人
     * @param subject      邮件主题
     * @param templateName 模板名
     * @param model        模板参数
     */
    public MimeMessage sendHtmlMail(String toEmail, String subject, String templateName, Map<String, Object> model) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        try {
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(username);
            Context context = new Context();
            context.setVariables(model);
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
        } catch (MessagingException e) {
            // 处理异常
            log.error("发送邮件失败：{}", e.getMessage());
        }
        return helper.getMimeMessage();
    }

}