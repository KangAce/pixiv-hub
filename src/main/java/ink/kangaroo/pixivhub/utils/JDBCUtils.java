package ink.kangaroo.pixivhub.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class JDBCUtils {
    //声明连接池对象
    private static DataSource dataSource;

    //使用静态，是类加载的时候就创建连接池
    static{
        try {
            //读取配置文件
            InputStream inputStream = JDBCUtils.class.getClassLoader().getResourceAsStream("druid.properties");
            //获取Properties对象，加载到该对象中
            Properties properties = new Properties();
            //获取配置文件
            properties.load(inputStream);
            //创建druid工厂
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取数据库连接
    public static Connection getConn() throws SQLException {
        return dataSource.getConnection();
    }


    /**
     * 关闭连接
     * 查询调用这个方法
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭连接
     * 增删改没有结果集
     */
    public static void close(Connection connection, Statement statement) {
        //直接调用上面的方法
        close(connection, statement, null);
    }

//    /**
//     * 通用的增方法
//     */
//    public static int insert(Object o,Class clazz){
//        Connection conn = null;
//        PreparedStatement ps = null;
//        //返回影响的行数
//        int row = 0;
//        try{
//            //获取连接
//            conn = getConn();
//            String tableName = clazz.getName();
//            clazz.getDeclaredFields();
//            //获取预编译对象
//            ps = conn.prepareStatement(sql);
//            //获取元数据，得到有多少占位符
//            ParameterMetaData metaData = ps.getParameterMetaData();
//            int count = metaData.getParameterCount();
//            //循环获取赋值
//            for (int i = 0; i < count; i++) {
//                ps.setObject(i+1,args[i]);
//            }
//            //执行SQL语句
//            row = ps.executeUpdate();
//        }catch (Exception e){
//            e.printStackTrace();
//        }finally {
//            close(conn,ps);
//        }
//        return row;
//    }
    /**
     * 通用的增删改方法
     */
    public static int update(String sql,Object...args){
        Connection conn = null;
        PreparedStatement ps = null;
        //返回影响的行数
        int row = 0;
        try{
            //获取连接
            conn = getConn();
            //获取预编译对象
            ps = conn.prepareStatement(sql);
            //获取元数据，得到有多少占位符
            ParameterMetaData metaData = ps.getParameterMetaData();
            int count = metaData.getParameterCount();
            //循环获取赋值
            for (int i = 0; i < count; i++) {
                ps.setObject(i+1,args[i]);
            }
            //执行SQL语句
            System.out.println(ps);
            row = ps.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn,ps);
        }
        return row;
    }

    /**
     * 通用的查询方法
     */
    public static <T> List<T> equery(String sql,Class<T>c,Object...args){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        //创建集合用于接收数据库中查的值
        List<T>list = new ArrayList<>();
        try{
            //获取连接
            conn = getConn();
            //获取预编译对象
            ps = conn.prepareStatement(sql);
            //通过获取元数据给占位符赋值
            ParameterMetaData metaData = ps.getParameterMetaData();
            int count = metaData.getParameterCount();
            for (int i = 0; i < count; i++) {
                ps.setObject(i+1,args[i]);
            }
            //执行sql
            rs = ps.executeQuery();
            //遍历集合，封装到集合中吗，一行数据封装一个对象
            while (rs.next()){
                //每条记录封装成一个对象
                T t = c.newInstance();
                //得到实体类中有哪些列名
                Field[] fields = c.getDeclaredFields();
                //遍历赋值
                for (Field field : fields) {
                    //获取列名
                    String name = field.getName();
                    //获取内容
                    Object value = rs.getObject(name);
                    //因为是私有的，要暴力反射
                    field.setAccessible(true);
                    //把最后得到的值赋值给创建的对象中
                    field.set(t,value);
                }
                //把最后含每一行值的对象添加到集合中
                list.add(t);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            close(conn,ps,rs);
        }
        return list;
    }


}

