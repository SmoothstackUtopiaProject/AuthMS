package com.utopia.auth.services;

import com.utopia.auth.models.MailRequest;
import com.utopia.auth.models.MailResponse;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.UnexpectedTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender sender;

  @Autowired
  private Configuration config;

  private static final Logger LOGGER = LoggerFactory.getLogger(
    EmailService.class
  );

  public MailResponse sendEmail(
    MailRequest request,
    Map<String, Object> model
  ) {
    LOGGER.info("EmailService... check");
    MailResponse response = new MailResponse();
    MimeMessage message = sender.createMimeMessage();
    try {
      // set mediaType
      MimeMessageHelper helper = new MimeMessageHelper(
        message,
        MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
        StandardCharsets.UTF_8.name()
      );

      Template t = config.getTemplate("email-template.ftl");
      String html = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);

      helper.setTo(request.getTo());
      helper.setText(html, true);
      helper.setSubject(request.getSubject());
      helper.setFrom(request.getFrom());
      sender.send(message);
      LOGGER.info("Sent recovery email");

      response.setMessage("mail send to : " + request.getTo());
      response.setStatus(Boolean.TRUE);
      LOGGER.info("Sent recovery email");
    } catch (Exception e) {
      LOGGER.error("Error recovery email: " + e.getMessage());
      response.setMessage("Mail Sending failure : " + e.getMessage());
      response.setStatus(Boolean.FALSE);
    }

    return response;
  }
}
