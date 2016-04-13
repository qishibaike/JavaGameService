package org.tont.util;

import java.io.InputStream;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class MyBatisUtil {
	
	private static String resource;
	private static SqlSessionFactory sessionFactory;

	static {
		//mybatis的配置文件
        resource = "MybatisConfig.xml";
        //使用类加载器加载mybatis的配置文件（它也加载关联的映射文件）
        InputStream is = MyBatisUtil.class.getClassLoader().getResourceAsStream(resource);
        //构建sqlSession的工厂
        sessionFactory = new SqlSessionFactoryBuilder().build(is);
	}
	
	public static SqlSession getSqlSession() {
		return sessionFactory.openSession();
	}
	
	public static SqlSession getSqlSession(boolean autoCommit) {
		return sessionFactory.openSession(autoCommit);
	}
}
