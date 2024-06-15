INSERT INTO strategy
VALUES (3, 100003, '抽奖策略-验证lock', NULL, '2024-01-13 10:34:06', '2024-01-13 10:34:06');



INSERT INTO `strategy_award` (`id`, `strategy_id`, `award_id`, `award_title`, `award_subtitle`, `award_count`,
                              `award_count_surplus`, `award_rate`, `rule_models`, `sort`, `create_time`, `update_time`)
VALUES (13, 100003, 107, '增加dall-e-3画图模型', '抽奖1次后解锁', 200, 200, 0.0400, 'rule_lock,rule_luck_award', 7,
        '2023-12-09 09:45:38', '2023-12-23 14:01:02'),
       (14, 100003, 108, '增加100次使用', '抽奖2次后解锁', 199, 199, 0.0099, 'rule_lock,rule_luck_award', 8,
        '2023-12-09 09:46:02', '2024-01-13 10:26:29'),
       (15, 100003, 109, '解锁全部模型', '抽奖6次后解锁', 1, 1, 0.0001, 'rule_lock,rule_luck_award', 9,
        '2023-12-09 09:46:39', '2023-12-09 12:20:50');


INSERT INTO `strategy_rule` (`id`, `strategy_id`, `award_id`, `rule_type`, `rule_model`, `rule_value`, `rule_desc`,
                             `create_time`, `update_time`)
VALUES (15, 100003, 107, 2, 'rule_lock', '1', '抽奖1次后解锁', '2023-12-09 10:16:41', '2023-12-09 12:55:53'),
       (16, 100003, 108, 2, 'rule_lock', '2', '抽奖2次后解锁', '2023-12-09 10:17:43', '2024-01-13 10:56:48'),
       (17, 100003, 109, 2, 'rule_lock', '6', '抽奖6次后解锁', '2023-12-09 10:17:43', '2023-12-09 12:55:54');