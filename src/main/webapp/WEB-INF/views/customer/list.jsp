<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>顧客一覧 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>顧客管理</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/customer?action=edit">新規登録</a>
                <a href="${pageContext.request.contextPath}/">ホーム</a>
            </nav>
        </header>

        <main>
            <!-- メッセージ表示 -->
            <c:if test="${not empty param.message}">
                <div class="alert alert-success">
                    ${param.message}
                </div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="alert alert-error">
                    ${param.error}
                </div>
            </c:if>

            <!-- 検索フォーム -->
            <div class="search-form">
                <form method="get" action="${pageContext.request.contextPath}/customer">
                    <input type="hidden" name="action" value="search">
                    <input type="text" name="keyword" placeholder="顧客名、コード、電話番号で検索" 
                           value="${param.keyword}" class="search-input">
                    <button type="submit" class="btn btn-primary">検索</button>
                    <c:if test="${not empty param.keyword}">
                        <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-secondary">クリア</a>
                    </c:if>
                </form>
            </div>

            <!-- 顧客一覧テーブル -->
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>顧客コード</th>
                            <th>氏名</th>
                            <th>性別</th>
                            <th>年齢</th>
                            <th>電話番号</th>
                            <th>メール</th>
                            <th>登録日</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty customers}">
                                <c:forEach var="customer" items="${customers}">
                                    <tr>
                                        <td>${customer.customerCode}</td>
                                        <td>${customer.lastName} ${customer.firstName}</td>
                                        <td>${customer.gender == 'M' ? '男' : '女'}</td>
                                        <td>${customer.age}歳</td>
                                        <td>${customer.phoneNumber}</td>
                                        <td>${customer.email}</td>
                                        <td>
                                            <fmt:formatDate value="${customer.createdAt}" pattern="yyyy/MM/dd" />
                                        </td>
                                        <td class="actions">
                                            <a href="${pageContext.request.contextPath}/customer?action=view&id=${customer.id}" class="btn btn-info btn-sm">詳細</a>
                                            <a href="${pageContext.request.contextPath}/customer?action=edit&id=${customer.id}" class="btn btn-warning btn-sm">編集</a>
                                            <a href="${pageContext.request.contextPath}/customer?action=delete&id=${customer.id}" 
                                               class="btn btn-danger btn-sm" 
                                               onclick="return confirm('本当に削除しますか？')">削除</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="text-center">顧客データがありません</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <c:if test="${not empty param.keyword and empty customers}">
                <div class="text-center">
                    <p>検索条件に一致する顧客が見つかりませんでした</p>
                </div>
            </c:if>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <script>
        // 自動でアラートを閉じる
        setTimeout(function() {
            var alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(alert) {
                alert.style.display = 'none';
            });
        }, 5000);
    </script>
</body>
</html>