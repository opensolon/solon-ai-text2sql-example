package com.wht.ai;

import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.noear.snack4.ONode;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.chat.message.UserMessage;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.repository.InMemoryRepository;
import org.noear.solon.ai.rag.splitter.TokenSizeTextSplitter;
import org.noear.solon.ai.rag.util.QueryCondition;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.sql.SqlUtils;
import org.noear.solon.test.HttpTester;
import org.noear.solon.test.SolonTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SolonTest(value = App.class)
public class AiTest extends HttpTester {

    @Inject
    ChatModel chatModel;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    SqlUtils sqlUtils;

    /**
     * 训练DDL
     */
    @SneakyThrows
    @Test
    public void trainDDL() {

        String ddl = "create table student\n" +
                "(\n" +
                "    id              bigint       not null comment '主键id',\n" +
                "    name            varchar(255) not null comment '姓名',\n" +
                "    age             int          not null comment '年龄',\n" +
                "    sex             tinyint      not null comment '性别;1:男性；2：女性',\n" +
                "    enrollment_time datetime     not null comment '入学时间',\n" +
                "    student_class   tinyint      not null comment '所属班级',\n" +
                "    create_time     datetime     not null comment '创建时间'\n" +
                ")\n" +
                "    comment '学生信息表';";

        InMemoryRepository repository = new InMemoryRepository(embeddingModel);

        //确保chunkSize足够大，不要让chunkSize过小分割了完整的ddl语句
        List<Document> ddlDocument = new TokenSizeTextSplitter(8192).split(ddl)
                .stream()
                .map(document -> document.metadata("type", "ddl"))
                .collect(Collectors.toList());
        repository.insert(ddlDocument);
    }

    /**
     * 训练SQL 有question
     */
    @SneakyThrows
    @Test
    public void trainSql1() {

        String question = "一年级有哪些学生";
        String sql = "select st.name, st.age,st.sex, st.enrollment_time\n" +
                "from student st inner join class_info ci\n" +
                "where st.student_class = ci.id\n" +
                "and ci.name = '一年级'";

        InMemoryRepository repository = new InMemoryRepository(embeddingModel);

        String data = new ONode().set("question", question).set("sql", sql).toJson();
        List<Document> ddlDocument = new TokenSizeTextSplitter(8192).split(data)
                .stream()
                .map(document -> document.metadata("type", "sql"))
                .collect(Collectors.toList());
        repository.insert(ddlDocument);
    }

    /**
     * 训练SQL 无question,自动生成一个问题
     */
    @SneakyThrows
    @Test
    public void trainSql2() {

        String sql = "select name, sex, age, enrollment_time, student_class, create_time from student";

        //构建消息
        SystemMessage systemMessage = new SystemMessage("The user will give you SQL and you will try to guess what the business question this query is answering. Return just the question without any additional explanation. Do not reference the table name in the question and use chinese.");
        UserMessage userMessage = new UserMessage(sql);
        String question = chatModel.prompt(systemMessage, userMessage)
                .call()
                .getMessage()
                .getResultContent();

        InMemoryRepository repository = new InMemoryRepository(embeddingModel);

        String data = new ONode().set("question", question).set("sql", sql).toJson();
        List<Document> ddlDocument = new TokenSizeTextSplitter(8192).split(data)
                .stream()
                .map(document -> document.metadata("type", "sql"))
                .collect(Collectors.toList());
        repository.insert(ddlDocument);
    }

    /**
     * 完整示例
     */
    @SneakyThrows
    @Test
    public void case1() {

        //1.训练DDL
        InMemoryRepository repository = new InMemoryRepository(embeddingModel);
        //1.1 学生表ddl
        String studentDDL = "create table student\n" +
                "(\n" +
                "    id              bigint       not null comment '主键id',\n" +
                "    name            varchar(255) not null comment '姓名',\n" +
                "    age             int          not null comment '年龄',\n" +
                "    sex             tinyint      not null comment '性别;1:男性；2：女性',\n" +
                "    enrollment_time datetime     not null comment '入学时间',\n" +
                "    student_class   tinyint      not null comment '所属班级',\n" +
                "    create_time     datetime     not null comment '创建时间'\n" +
                ")\n" +
                "    comment '学生信息表';";
        //确保chunkSize足够大，不要让chunkSize过小分割了完整的ddl语句
        List<Document> studentDDLDocument = new TokenSizeTextSplitter(8192).split(studentDDL)
                .stream()
                .map(document -> document.metadata("title", "ddl"))
                .collect(Collectors.toList());
        repository.insert(studentDDLDocument);

        //1.2 教师表sql
        String teacherDDL = "create table teacher\n" +
                "(\n" +
                "    id            bigint       not null comment '主键id',\n" +
                "    name          varchar(255) not null comment '姓名',\n" +
                "    age           int          not null comment '年龄',\n" +
                "    sex           tinyint      not null comment '性别;1:男性；2：女性',\n" +
                "    teaching_time int          not null comment '教学年限',\n" +
                "    create_time   datetime     not null comment '创建时间'\n" +
                ")\n" +
                "    comment '教师信息表';";
        //确保chunkSize足够大，不要让chunkSize过小分割了完整的ddl语句
        List<Document> teacherDDLDocument = new TokenSizeTextSplitter(8192).split(teacherDDL)
                .stream()
                .map(document -> document.metadata("title", "ddl"))
                .collect(Collectors.toList());
        repository.insert(studentDDLDocument);

        //1.3 班级表sql
        String classDDL = "create table class_info\n" +
                "(\n" +
                "    id               bigint       not null comment '主键id',\n" +
                "    class_id         tinyint      null comment '班级id',\n" +
                "    name             varchar(255) not null comment '班级名',\n" +
                "    class_teacher_id bigint       not null comment '教师id',\n" +
                "    create_time      datetime     not null comment '创建时间'\n" +
                ")\n" +
                "    comment '班级信息表'";
        //确保chunkSize足够大，不要让chunkSize过小分割了完整的ddl语句
        List<Document> classDDLDocument = new TokenSizeTextSplitter(8192).split(classDDL)
                .stream()
                .map(document -> document.metadata("title", "ddl"))
                .collect(Collectors.toList());
        repository.insert(studentDDLDocument);

        //2.直接训练sql
        //2.1 有sql和问题
        String question1 = "一年级有哪些学生";
        String sql1 = "select st.name, st.age,st.sex, st.enrollment_time\n" +
                "from student st inner join class_info ci\n" +
                "where st.student_class = ci.class_id\n" +
                "and ci.name = '一年级'";
        String data1 = new ONode().set("question", question1).set("sql", sql1).toJson();
        List<Document> ddlDocument1 = new TokenSizeTextSplitter(8192).split(data1)
                .stream()
                .map(document -> document.metadata("type", "sql"))
                .collect(Collectors.toList());
        repository.insert(ddlDocument1);

        //2.2 只有sql,没有问题
        String sql2 = "select name, sex, age, enrollment_time, student_class, create_time from student";
        //构建消息
        SystemMessage systemMessage = new SystemMessage("The user will give you SQL and you will try to guess what the business question this query is answering. Return just the question without any additional explanation. Do not reference the table name in the question and use chinese.");
        UserMessage userMessage = new UserMessage(sql2);
        String question2 = chatModel.prompt(systemMessage, userMessage)
                .call()
                .getMessage()
                .getResultContent();

        String data2 = new ONode().set("question", question2).set("sql", sql2).toJson();
        List<Document> ddlDocument2 = new TokenSizeTextSplitter(8192).split(data2)
                .stream()
                .map(document -> document.metadata("type", "sql"))
                .collect(Collectors.toList());
        repository.insert(ddlDocument2);

        //3.生成sql
        String prompt = "一年级有哪些男学生";

        //3.1 获取ddl
        List<Document> ddlSearch = repository.search(new QueryCondition(prompt).filterExpression("title == 'ddl'").disableRefilter(false));

        //3.2 获取sql
        List<Document> sqlSearch = repository.search(new QueryCondition(prompt).filterExpression("title == 'sql'").disableRefilter(false));

        //3.3 构建消息
        String sysMessage = StrUtil.format(
                "===Response Guidelines \n" +
                        "1. If the provided context is sufficient, please generate a valid SQL query without any explanations for the question. \n" +
                        "2. If the provided context is almost sufficient but requires knowledge of a specific string in a particular column, please generate an intermediate SQL query to find the distinct strings in that column. Prepend the query with a comment saying intermediate_sql \n" +
                        "3. If the provided context is insufficient, please explain why it can't be generated. " +
                        "4. Please use the most relevant table(s). \n" +
                        "5. If the question has been asked and answered before, please repeat the answer exactly as it was given before. \n" +
                        "6. Ensure that the output SQL is {} -compliant and executable, and free of syntax errors. \n" +
                        "7.Please refer to the DDL statement:{}\n" +
                        "8.Please refer to the SQL statement:{}",
                "MYSQL", ddlSearch, sqlSearch
        );

        //3.4 提交大模型生成sql语句
        String sql = chatModel.prompt(new SystemMessage(sysMessage), new UserMessage(prompt)).call()
                .getMessage()
                .getResultContent();

        System.err.println(sql);

        List<Map> result = sqlUtils.sql(sql).queryRowList(Map.class);

        System.err.println(result);


    }


}
