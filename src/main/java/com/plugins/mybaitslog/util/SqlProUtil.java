package com.plugins.mybaitslog.util;

import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 用于SQL的还原
 *
 * @author lk
 * @version 1.0
 * @date 2020/8/23 17:14
 */
public class SqlProUtil {

    private final static BasicFormatter BASIC_FORMATTER = new BasicFormatter();
    private final static Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\(([^\\(]+)\\)$");
    /**
     * 获取Sql语句类型
     *
     * @param sql 语句
     * @return string
     */
    public static String getSqlType(String sql) {
        if (StringUtils.isNotBlank(sql)) {
            String lowerLine = sql.toLowerCase().trim();
            if (lowerLine.startsWith("insert")) {
                return "insert";
            }
            if (lowerLine.startsWith("update")) {
                return "update";
            }
            if (lowerLine.startsWith("delete")) {
                return "delete";
            }
            if (lowerLine.startsWith("select")) {
                return "select";
            }
        }
        return "";
    }

    /**
     * Sql语句还原，整个插件的核心就是该方法
     *
     * @param preparingLine  sql
     * @param parametersLine 参数
     * @return string[]
     */
    public static String[] restoreSql(Project project, final String preparingLine, final String parametersLine) {
        final String PREPARING = ConfigUtil.getPreparing(project);
        final String PARAMETERS = ConfigUtil.getParameters(project);
        final String[] preparingLineSplit = preparingLine.split(PREPARING);
        final String[] parametersLineSplit = parametersLine.split(PARAMETERS);
        final String[] preparing = getPreparing(preparingLineSplit);
        final Object[] parameters = getParameters(parametersLineSplit);
        final String sqlformat = String.format(preparing[1], parameters);
        final String result = BASIC_FORMATTER.format(sqlformat);
        return new String[]{preparing[0], result};
    }

    private static String[] getPreparing(String[] preparingLineSplit) {
        String one = "";
        String tow = "";
        if (preparingLineSplit.length == 2) {
            one = preparingLineSplit[0];
            tow = preparingLineSplit[1].replace("?", "%s");
        }
        return new String[]{one, tow};
    }

    private static Object[] getParameters(String[] parametersLineSplit) {
        if (parametersLineSplit.length == 2) {
            final String[] split = parametersLineSplit[1].split(",");
            final List<String> params = new ArrayList<>();
            for (String value : split) {
                String s = value.trim();
                Matcher matcher = PARAMETER_TYPE_PATTERN.matcher(s);
                String groupHole = null;
                String group = null;
                if (matcher.find()) {
                    group = matcher.group(1);
                    groupHole = matcher.group(0);
                }
                if (group != null) {
                    if ("String".equals(group)
                            || "Timestamp".equals(group)
                            || "Date".equals(group)
                            || "Time".equals(group)
                            || "Calendar".equals(group)
                            || "Currency".equals(group)
                            || "TimeZone".equals(group)
                            || "Locale".equals(group)
                            || "Class".equals(group)
                    ) {
                        params.add("'" + s.replace(groupHole, "") + "'");
                    } else if ("Boolean".equals(group)) {
                        s = s.replace(groupHole, "");
                        if ("true".equalsIgnoreCase(s)) {
                            s = "1";
                        } else if ("false".equalsIgnoreCase(s)) {
                            s = "0";
                        }
                        params.add(s);
                    } else {
                        params.add(s.replace(groupHole, ""));
                    }
                } else {
                    params.add(s);
                }
            }
            return params.toArray(new String[0]);
        }
        return new String[]{};
    }


}
