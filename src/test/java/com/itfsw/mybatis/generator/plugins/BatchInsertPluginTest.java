/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/6/26 18:23
 * ---------------------------------------------------------------------------
 */
public class BatchInsertPluginTest {
    private DBHelper helper;

    /**
     * 初始化
     * @throws IOException
     * @throws SQLException
     */
    @Before
    public void init() throws IOException, SQLException {
        helper = DBHelper.getHelper("scripts/BatchInsertPlugin/init.sql");
    }

    /**
     * 测试插件依赖
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public void testWarnings1() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/BatchInsertPlugin/mybatis-generator-without-model-column-plugin.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, false);

        Assert.assertTrue(warnings.size() == 2);
        Assert.assertEquals(warnings.get(0), "itfsw:插件com.itfsw.mybatis.generator.plugins.BatchInsertPlugin插件需配合com.itfsw.mybatis.generator.plugins.ModelColumnPlugin插件使用！");
    }

    /**
     * 测试错误的支持driver
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     */
    @Test
    public void testWarnings2() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/BatchInsertPlugin/mybatis-generator-with-error-driver.xml"));

        DefaultShellCallback shellCallback = new DefaultShellCallback(true);
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, shellCallback, warnings);
        myBatisGenerator.generate(null, null, null, false);

        Assert.assertTrue(warnings.size() == 3);
        Assert.assertEquals(warnings.get(1), "itfsw:插件com.itfsw.mybatis.generator.plugins.BatchInsertPlugin插件使用前提是数据库为MySQL或者SQLserver，因为返回主键使用了JDBC的getGenereatedKeys方法获取主键！");
    }

    /**
     * 测试生成的方法
     * @throws IOException
     * @throws XMLParserException
     * @throws InvalidConfigurationException
     * @throws SQLException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    @Test
    public void testMethods() throws IOException, XMLParserException, InvalidConfigurationException, SQLException, InterruptedException, ClassNotFoundException, NoSuchMethodException {
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/BatchInsertPlugin/mybatis-generator.xml"));

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new AbstractShellCallback(true) {
            @Override
            public void reloadProject(ClassLoader loader) {
                try {
                    // 1. 普通Mapper参数中List泛型为普通Model
                    Class clsTbMapper = loader.loadClass("com.itfsw.mybatis.generator.plugins.dao.TbMapper");
                    int count = 0;
                    for (Method method : clsTbMapper.getDeclaredMethods()) {
                        if (method.getName().equals("batchInsert")) {
                            Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), "com.itfsw.mybatis.generator.plugins.dao.model.Tb");
                            count++;
                        }
                        if (method.getName().equals("batchInsertSelective")) {
                            Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), "com.itfsw.mybatis.generator.plugins.dao.model.Tb");
                            Assert.assertEquals(method.getGenericParameterTypes()[1].getTypeName(), "com.itfsw.mybatis.generator.plugins.dao.model.Tb$Column[]");
                            count++;
                        }
                    }
                    Assert.assertEquals(count, 2);

                    // 2. 带有WithBlobs
                    Class clsTbBlobsMapper = loader.loadClass("com.itfsw.mybatis.generator.plugins.dao.TbBlobsMapper");
                    count = 0;
                    for (Method method : clsTbBlobsMapper.getDeclaredMethods()) {
                        if (method.getName().equals("batchInsert")) {
                            Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs");
                            count++;
                        }
                        if (method.getName().equals("batchInsertSelective")) {
                            Assert.assertEquals(Util.getListActualType(method.getGenericParameterTypes()[0]), "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs");
                            Assert.assertEquals(method.getGenericParameterTypes()[1].getTypeName(), "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs$Column[]");
                            count++;
                        }
                    }
                    Assert.assertEquals(count, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                }
            }
        }, warnings);
        myBatisGenerator.generate(null, null, null, true);
    }

    /**
     * 测试生成的sql
     */
    @Test
    public void testBatchInsert() throws Exception {
        DBHelper.cleanDao();
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/BatchInsertPlugin/mybatis-generator.xml"));

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new AbstractShellCallback(true) {
            @Override
            public void reloadProject(ClassLoader loader) {
                SqlSession sqlSession = null;
                try {
                    // 1. 测试sql
                    sqlSession = helper.getSqlSession();
                    ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass("com.itfsw.mybatis.generator.plugins.dao.TbMapper")));
                    List<Object> params = new ArrayList<>();
                    params.add(
                            new ObjectUtil(loader, "com.itfsw.mybatis.generator.plugins.dao.model.Tb")
                                    .set("field1", "test")
                                    .getObject()
                    );
                    params.add(
                            new ObjectUtil(loader, "com.itfsw.mybatis.generator.plugins.dao.model.Tb")
                                    .set("field1", "test")
                                    .set("field2", 1)
                                    .getObject()
                    );
                    String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "batchInsert", params);
                    Assert.assertEquals(sql, "insert into tb (field1, field2) values ('test', null) ,  ('test', 1)");
                    // 2. 执行sql
                    Object count = tbMapper.invoke("batchInsert", params);
                    Assert.assertEquals(count, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                } finally {
                    sqlSession.close();
                }
            }
        }, warnings);
        myBatisGenerator.generate(null, null, null, true);
    }

    /**
     * 测试生成的sql
     */
    @Test
    public void testBatchInsertSelective() throws Exception {
        DBHelper.cleanDao();
        List<String> warnings = new ArrayList<>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(Resources.getResourceAsStream("scripts/BatchInsertPlugin/mybatis-generator.xml"));

        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, new AbstractShellCallback(true) {
            @Override
            public void reloadProject(ClassLoader loader) {
                SqlSession sqlSession = null;
                try {
                    // 1. 测试sql
                    sqlSession = helper.getSqlSession();
                    ObjectUtil tbBlobsMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass("com.itfsw.mybatis.generator.plugins.dao.TbBlobsMapper")));
                    List<Object> params = new ArrayList<>();
                    params.add(
                            new ObjectUtil(loader, "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs")
                                    .set("field1", "test")
                                    .getObject()
                    );
                    params.add(
                            new ObjectUtil(loader, "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs")
                                    .set("field1", "test")
                                    .set("field2", "test123")
                                    .getObject()
                    );
                    ObjectUtil columnField2 = new ObjectUtil(loader, "com.itfsw.mybatis.generator.plugins.dao.model.TbBlobsWithBLOBs$Column#field2");
                    // java 动态参数不能有两个会冲突，最后一个封装成Array!!!必须使用反射创建指定类型数组，不然调用invoke对了可变参数会检查类型！
                    Object columns = Array.newInstance(columnField2.getCls(), 1);
                    Array.set(columns, 0, columnField2.getObject());

                    String sql = SqlHelper.getFormatMapperSql(tbBlobsMapper.getObject(), "batchInsertSelective", params, columns);
                    Assert.assertEquals(sql, "insert into tb_blobs ( field2 ) values ( 'null' ) ,  ( 'test123' )");
                    // 2. 执行sql
                    Object count = tbBlobsMapper.invokeVarArgs("batchInsertSelective", params, columns);
                    Assert.assertEquals(count, 2);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.assertTrue(false);
                } finally {
                    sqlSession.close();
                }
            }
        }, warnings);
        myBatisGenerator.generate(null, null, null, true);
    }

    @AfterClass
    public static void clean() {
        DBHelper.reset();
    }
}
