package cn.learn.domain.activity.service;

import cn.learn.domain.activity.repository.IActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 抽奖活动服务
 * @create 2024-03-16 08:41
 */
@Service
public class RaffleActivityService extends AbstractRaffleActivity {

    public RaffleActivityService(IActivityRepository activityRepository) {
        super(activityRepository);
    }

}
