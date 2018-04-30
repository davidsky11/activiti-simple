package com.kvlt;

import com.kvlt.domain.UserInfo;
import com.kvlt.mapper.UserInfoMapper;
import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * @author daishengkai
 * 2018-04-21 10:52
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ActivitiApplication.class)
@Slf4j
public class AppTest {

    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);

    @Resource
    private UserInfoMapper userInfoMapper;

    @Test
    public void testDynamicDatasource() {
        UserInfo userInfo;
        for (int i = 1; i <= 2; i++) {
            //i为奇数时调用selectByOddUserId方法获取，i为偶数时调用selectByEvenUserId方法获取
            userInfo = i % 2 == 1 ? userInfoMapper.selectByOddUserId(i) : userInfoMapper.selectByEvenUserId(i);
            logger.info("{}->={}", userInfo.getId(), userInfo.getRemarks());
        }
    }
}
