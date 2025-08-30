<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>システムログ管理 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="container">
        <!-- ヘッダー -->
        <header class="header">
            <h1>システムログ管理</h1>
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/admin">管理者ダッシュボード</a> &gt; システムログ
            </nav>
        </header>

        <!-- メッセージ表示 -->
        <c:if test="${not empty message}">
            <div class="alert alert-${messageType}">
                ${message}
            </div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-error">
                ${error}
            </div>
        </c:if>

        <div class="admin-content">
            <!-- 検索フォーム -->
            <div class="card">
                <div class="card-header">
                    <h3>ログ検索</h3>
                </div>
                <div class="card-body">
                    <form method="get" action="${pageContext.request.contextPath}/admin/logs">
                        <input type="hidden" name="action" value="search">
                        <div class="form-row">
                            <div class="form-group">
                                <label>ログレベル:</label>
                                <select name="level" class="form-control">
                                    <option value="">すべて</option>
                                    <option value="INFO" ${searchLevel == 'INFO' ? 'selected' : ''}>INFO</option>
                                    <option value="WARN" ${searchLevel == 'WARN' ? 'selected' : ''}>WARN</option>
                                    <option value="ERROR" ${searchLevel == 'ERROR' ? 'selected' : ''}>ERROR</option>
                                    <option value="DEBUG" ${searchLevel == 'DEBUG' ? 'selected' : ''}>DEBUG</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label>モジュール:</label>
                                <input type="text" name="module" value="${searchModule}" class="form-control" placeholder="モジュール名">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>キーワード:</label>
                                <input type="text" name="keyword" value="${searchKeyword}" class="form-control" placeholder="メッセージまたはユーザー名">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>開始日:</label>
                                <input type="date" name="startDate" value="${searchStartDate}" class="form-control">
                            </div>
                            <div class="form-group">
                                <label>終了日:</label>
                                <input type="date" name="endDate" value="${searchEndDate}" class="form-control">
                            </div>
                        </div>
                        <div class="form-group text-right">
                            <button type="submit" class="btn btn-primary">検索</button>
                            <a href="${pageContext.request.contextPath}/admin/logs" class="btn btn-secondary">クリア</a>
                        </div>
                    </form>
                </div>
            </div>

            <!-- ログ一覧 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>ログ一覧 <small>全 ${totalCount} 件</small></h3>
                    <div class="header-actions">
                        <a href="${pageContext.request.contextPath}/admin/logs?action=statistics" class="btn btn-info btn-sm">統計表示</a>
                        <button type="button" class="btn btn-warning btn-sm" onclick="showCleanupDialog()">古いログを削除</button>
                    </div>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty logs}">
                            <div class="table-responsive">
                                <table class="table">
                                    <thead>
                                        <tr>
                                            <th>時間</th>
                                            <th>レベル</th>
                                            <th>モジュール</th>
                                            <th>メッセージ</th>
                                            <th>ユーザー</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${logs}">
                                            <tr class="log-level-${log.logLevel.toLowerCase()}">
                                                <td><fmt:formatDate value="${log.logTime}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                                                <td>
                                                    <span class="badge badge-${log.logLevel.toLowerCase()}">
                                                        ${log.logLevel}
                                                    </span>
                                                </td>
                                                <td>${log.module}</td>
                                                <td class="log-message">
                                                    <c:choose>
                                                        <c:when test="${fn:length(log.logMessage) > 100}">
                                                            ${fn:substring(log.logMessage, 0, 100)}...
                                                        </c:when>
                                                        <c:otherwise>
                                                            ${log.logMessage}
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>${not empty log.username ? log.username : 'システム'}</td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/admin/logs?action=view&id=${log.id}" class="btn btn-sm btn-info">詳細</a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="empty-state">
                                <c:choose>
                                    <c:when test="${isSearch}">
                                        <p>検索条件に一致するログが見つかりませんでした。</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p>ログがありません。</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>

    <!-- クリーンアップダイアログ -->
    <div id="cleanupDialog" class="modal" style="display: none;">
        <div class="modal-content">
            <h3>古いログを削除</h3>
            <form method="post" action="${pageContext.request.contextPath}/admin/logs">
                <input type="hidden" name="action" value="cleanup">
                <div class="form-group">
                    <label>保持する日数:</label>
                    <input type="number" name="days" value="30" min="1" max="365" class="form-control">
                    <small>この日数より古いログを削除します</small>
                </div>
                <div class="form-group text-right">
                    <button type="button" class="btn btn-secondary" onclick="hideCleanupDialog()">キャンセル</button>
                    <button type="submit" class="btn btn-warning">削除</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function showCleanupDialog() {
            document.getElementById('cleanupDialog').style.display = 'block';
        }
        
        function hideCleanupDialog() {
            document.getElementById('cleanupDialog').style.display = 'none';
        }
        
        // モーダル外をクリックで閉じる
        window.onclick = function(event) {
            var modal = document.getElementById('cleanupDialog');
            if (event.target === modal) {
                hideCleanupDialog();
            }
        }
    </script>

    <!-- フッター -->
    <footer class="footer">
        <p>&copy; 2025 保険システム - システムログ管理</p>
    </footer>
</body>
</html>