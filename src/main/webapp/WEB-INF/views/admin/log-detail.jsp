<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ログ詳細 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="container">
        <!-- ヘッダー -->
        <header class="header">
            <h1>ログ詳細</h1>
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/admin">管理者ダッシュボード</a> &gt;
                <a href="${pageContext.request.contextPath}/admin/logs">システムログ</a> &gt; 詳細
            </nav>
        </header>

        <div class="admin-content">
            <div class="card">
                <div class="card-header">
                    <h3>ログ情報</h3>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/admin/logs" class="btn btn-secondary btn-sm">戻る</a>
                    </div>
                </div>
                <div class="card-body">
                    <div class="log-detail">
                        <div class="detail-row">
                            <label>ログID:</label>
                            <span>${log.id}</span>
                        </div>
                        <div class="detail-row">
                            <label>レベル:</label>
                            <span class="badge badge-${log.logLevel.toLowerCase()}">${log.logLevel}</span>
                        </div>
                        <div class="detail-row">
                            <label>時間:</label>
                            <span><fmt:formatDate value="${log.logTime}" pattern="yyyy-MM-dd HH:mm:ss" /></span>
                        </div>
                        <div class="detail-row">
                            <label>モジュール:</label>
                            <span>${log.module}</span>
                        </div>
                        <div class="detail-row">
                            <label>ユーザー:</label>
                            <span>${not empty log.username ? log.username : 'システム'}</span>
                        </div>
                        <div class="detail-row">
                            <label>IPアドレス:</label>
                            <span>${not empty log.ipAddress ? log.ipAddress : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <label>リクエストID:</label>
                            <span>${not empty log.requestId ? log.requestId : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <label>セッションID:</label>
                            <span>${not empty log.sessionId ? log.sessionId : 'N/A'}</span>
                        </div>
                        <div class="detail-row">
                            <label>ユーザーエージェント:</label>
                            <span>${not empty log.userAgent ? log.userAgent : 'N/A'}</span>
                        </div>
                        <div class="detail-row full-width">
                            <label>メッセージ:</label>
                            <div class="log-message-content">
                                ${log.logMessage}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- フッター -->
    <footer class="footer">
        <p>&copy; 2025 保険システム - ログ詳細</p>
    </footer>
</body>
</html>