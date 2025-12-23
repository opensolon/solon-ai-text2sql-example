package com.wht.ai.api;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import lombok.SneakyThrows;
import org.noear.snack4.ONode;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.message.SystemMessage;
import org.noear.solon.ai.chat.message.UserMessage;
import org.noear.solon.ai.embedding.EmbeddingModel;
import org.noear.solon.ai.rag.Document;
import org.noear.solon.ai.rag.repository.InMemoryRepository;
import org.noear.solon.ai.rag.splitter.TokenSizeTextSplitter;
import org.noear.solon.ai.rag.util.QueryCondition;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.data.sql.SqlUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author by HaiTao.Wang on 2025/5/28.
 */
@Mapping("/api")
@Controller
public class ApiController {

    @Inject
    ChatModel chatModel;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    SqlUtils sqlUtils;

    /**
     * 训练接口
     *
     * @param ddl      建表ddl语句，要求全注释
     * @param sql      sql语句
     * @param question 问题
     * @return
     */
    @SneakyThrows
    @Mapping("train")
    public void train(String ddl, String sql, String question) {

        InMemoryRepository repository = new InMemoryRepository(embeddingModel);

        //1.直接训练ddl
        if (StrUtil.isNotEmpty(ddl)) {
            //确保chunkSize足够大，不要让chunkSize过小分割了完整的ddl语句
            List<Document> ddlDocument = new TokenSizeTextSplitter(8192).split(ddl)
                    .stream()
                    .map(document -> document.metadata("type", "ddl"))
                    .collect(Collectors.toList());
            repository.insert(ddlDocument);
        }

        //2.直接训练sql 此方法可以用作持续优化
        if (StrUtil.isNotEmpty(sql)) {
            if (StrUtil.isEmpty(question)) {
                //构建消息
                SystemMessage systemMessage = new SystemMessage("The user will give you SQL and you will try to guess what the business question this query is answering. Return just the question without any additional explanation. Do not reference the table name in the question and use chinese.");
                UserMessage userMessage = new UserMessage(sql);
                question = chatModel.prompt(systemMessage, userMessage)
                        .call()
                        .getMessage()
                        .getResultContent();
            }

            String data = new ONode().set("question", question).set("sql", sql).toJson();
            List<Document> ddlDocument = new TokenSizeTextSplitter(8192).split(data)
                    .stream()
                    .map(document -> document.metadata("type", "sql"))
                    .collect(Collectors.toList());
            repository.insert(ddlDocument);


            //数据存入数据库(可选)
            sqlUtils.sql("insert into train_question (question, answer_sql, create_time) values (?,?,?)", question, sql, DateUtil.now()).update();
        }
    }

    /**
     * 生成sql接口
     *
     * @param prompt
     * @return
     */
    @SneakyThrows
    @Mapping("generateSql")
    public String generateSql(String prompt) {

        InMemoryRepository repository = new InMemoryRepository(embeddingModel);
        //1.获取ddl
        List<Document> ddlSearch = repository.search(new QueryCondition(prompt).filterExpression("type == 'ddl'").disableRefilter(true));

        //2.获取sql
        List<Document> sqlSearch = repository.search(new QueryCondition(prompt).filterExpression("type == 'sql'").disableRefilter(true));

        //3.构建消息
        String sysMessage = StrUtil.format("===Response Guidelines \n" +
                "1. If the provided context is sufficient, please generate a valid SQL query without any explanations for the question. \n" +
                "2. If the provided context is almost sufficient but requires knowledge of a specific string in a particular column, please generate an intermediate SQL query to find the distinct strings in that column. Prepend the query with a comment saying intermediate_sql \n" +
                "3. If the provided context is insufficient, please explain why it can't be generated. " +
                "4. Please use the most relevant table(s). \n" +
                "5. If the question has been asked and answered before, please repeat the answer exactly as it was given before. \n" +
                "6. Ensure that the output SQL is {} -compliant and executable, and free of syntax errors. \n" +
                "7.Please refer to the DDL statement:{}\n" +
                "8.Please refer to the SQL statement:{}", "MYSQL", ddlSearch, sqlSearch);

        //4. 提交大模型
        return chatModel.prompt(new SystemMessage(sysMessage), new UserMessage(prompt)).call()
                .getMessage()
                .getResultContent();

    }

}
