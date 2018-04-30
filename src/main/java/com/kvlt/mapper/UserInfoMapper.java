package com.kvlt.mapper;

import com.kvlt.datasource.annotation.TargetDataSource;
import com.kvlt.domain.UserInfo;

public interface UserInfoMapper {

	/**
	 * 从test1数据源中获取用户信息
	 */
	@TargetDataSource("master")
	UserInfo selectByOddUserId(Integer id);

	/**
	 * 从test2数据源中获取用户信息
	 */
	@TargetDataSource("slave")
	UserInfo selectByEvenUserId(Integer id);

}