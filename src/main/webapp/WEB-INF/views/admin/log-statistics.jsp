<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ログ統計 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="container">
        <!-- ヘッダー -->
        <header class="header">
            <h1>ログ統計情報</h1>
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/admin">管理者ダッシュボード</a> &gt;
                <a href="${pageContext.request.contextPath}/admin/logs">システムログ</a> &gt; 統計
            </nav>
        </header>

        <div class="admin-content">
            <!-- 概要統計 -->
            <div class="card">
                <div class="card-header">
                    <h3>概要統計</h3>
                </div>
                <div class="card-body">
                    <div class="stat-summary">
                        <div class="stat-item">
                            <div class="stat-number">${totalLogs}</div>
                            <div class="stat-label">総ログ数</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- レベル別統計 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>ログレベル別統計</h3>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>レベル</th>
                                    <th>件数</th>
                                    <th>割合</th>
                                    <th>バッジ</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="stat" items="${levelStats}">
                                    <c:set var="percentage" value="${stat[1] * 100.0 / totalLogs}" />
                                    <tr>
                                        <td>${stat[0]}</td>
                                        <td>${stat[1]} 件</td>
                                        <td>
                                            <div class="progress">
                                                <div class="progress-bar progress-${stat[0].toString().toLowerCase()}" 
                                                     style="width: ${percentage}%">
                                                    <fmt:formatNumber value="${percentage}" pattern="0.0" />%
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            <span class="badge badge-${stat[0].toString().toLowerCase()}">
                                                ${stat[0]}
                                            </span>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- モジュール別統計 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>モジュール別統計</h3>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty moduleStats}">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>モジュール</th>
                                            <th>件数</th>
                                            <th>割合</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="stat" items="${moduleStats}">
                                            <c:set var="percentage" value="${stat[1] * 100.0 / totalLogs}" />
                                            <tr>
                                                <td>${stat[0]}</td>
                                                <td>${stat[1]} 件</td>
                                                <td>
                                                    <div class="progress">
                                                        <div class="progress-bar" 
                                                             style="width: ${percentage}%">
                                                            <fmt:formatNumber value="${percentage}" pattern="0.0" />%
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <p>モジュール統計データがありません。</p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- アクションボタン -->
            <div class="text-center mt-4">
                <a href="${pageContext.request.contextPath}/admin/logs" class="btn btn-primary">ログ一覧に戻る</a>
                <button onclick="window.print()" class="btn btn-secondary">印刷</button>
            </div>
        </div>
    </div>

    <!-- フッター -->
    <footer class="footer">
        <p>&copy; 2025 保険システム - ログ統計</p>
    </footer>
</body>
</html>