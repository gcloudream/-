package com.silemore.task;

import com.silemore.entity.User;
import com.silemore.repository.UserRepository;
import com.silemore.service.CheckInService;
import com.silemore.service.EmailService;
import com.silemore.service.UserService;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderTask {
    private final UserRepository userRepository;
    private final CheckInService checkInService;
    private final EmailService emailService;
    private final UserService userService;

    public ReminderTask(UserRepository userRepository,
                        CheckInService checkInService,
                        EmailService emailService,
                        UserService userService) {
        this.userRepository = userRepository;
        this.checkInService = checkInService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @Scheduled(cron = "0 0 20 * * ?")
    public void sendDailyReminders() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            user = userService.normalizePauseIfExpired(user);
            if (Boolean.TRUE.equals(user.getIsPaused())) {
                continue;
            }

            if (!Boolean.TRUE.equals(user.getReminderEnabled())) {
                continue;
            }
            if (!checkInService.hasCheckedInToday(user.getId())) {
                try {
                    emailService.sendReminderEmail(user);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
