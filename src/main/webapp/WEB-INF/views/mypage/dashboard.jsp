<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>マイページ - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/mypage.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>マイページ</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/mypage?action=dashboard" class="${activeTab == 'dashboard' ? 'active' : ''}">ダッシュボード</a>
                <a href="${pageContext.request.contextPath}/mypage?action=profile" class="${activeTab == 'profile' ? 'active' : ''}">プロフィール</a>
                <a href="${pageContext.request.contextPath}/mypage?action=contracts" class="${activeTab == 'contracts' ? 'active' : ''}">契約情報</a>
                <a href="${pageContext.request.contextPath}/mypage?action=documents" class="${activeTab == 'documents' ? 'active' : ''}">資料請求</a>
                <a href="${pageContext.request.contextPath}/mypage?action=premiums" class="${activeTab == 'premiums' ? 'active' : ''}">保険料支払い</a>
                <a href="${pageContext.request.contextPath}/mypage?action=settings" class="${activeTab == 'settings' ? 'active' : ''}">設定</a>
                <a href="${pageContext.request.contextPath}/">ログアウト</a>
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

            <div class="mypage-content">
                <div class="welcome-section">
                    <h2>ようこそ、${customer.lastName} ${customer.firstName} さん</h2>
                    <p>最終ログイン: <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy年MM月dd日 HH:mm" /></p>
                </div>

                <div class="dashboard-grid">
                    <!-- 契約状況 -->
                    <div class="dashboard-card">
                        <h3>契約状況</h3>
                        <div class="stat-number">${not empty contracts ? contracts.size() : 0}</div>
                        <p>現在の契約数</p>
                        <a href="${pageContext.request.contextPath}/mypage?action=contracts" class="btn btn-primary btn-sm">詳細を見る</a>
                    </div>

                    <!-- 資料請求状況 -->
                    <div class="dashboard-card">
                        <h3>資料請求</h3>
                        <div class="stat-number">${not empty documentRequests ? documentRequests.size() : 0}</div>
                        <p>リクエスト数</p>
                        <a href="${pageContext.request.contextPath}/mypage?action=documents" class="btn btn-primary btn-sm">詳細を見る</a>
                    </div>

                    <!-- 次の支払い -->
                    <div class="dashboard-card">
                        <h3>次の保険料支払い</h3>
                        <div class="stat-number">-</div>
                        <p>近日中に支払い予定はありません</p>
                        <a href="${pageContext.request.contextPath}/mypage?action=premiums" class="btn btn-primary btn-sm">支払い履歴</a>
                    </div>

                    <!-- お知らせ -->
                    <div class="dashboard-card">
                        <h3>お知らせ</h3>
                        <div class="notifications">
                            <div class="notification">
                                <span class="notification-date">2024/08/28</span>
                                <p>システムメンテナンスのご案内</p>
                            </div>
                            <div class="notification">
                                <span class="notification-date">2024/08/25</span>
                                <p>新しい保険商品のご案内</p>
                            </div>
                        </div>
                        <a href="#" class="btn btn-secondary btn-sm">すべて見る</a>
                    </div>
                </div>

                <!-- 最近の活動 -->
                <div class="recent-activity">
                    <h3>最近の活動</h3>
                    <div class="activity-list">
                        <c:if test="${not empty documentRequests}">
                            <c:forEach var="request" items="${documentRequests}" end="4">
                                <div class="activity-item">
                                    <span class="activity-date">
                                        <fmt:formatDate value="${request.createdAt}" pattern="MM/dd HH:mm" />
                                    </span>
                                    <span class="activity-type">資料請求</span>
                                    <span class="activity-details">
                                        ${request.requestType} - ${request.requestStatus}
                                    </span>
                                </div>
                            </c:forEach>
                        </c:if>
                        <c:if test="${empty documentRequests}">
                            <p class="no-activity">最近の活動はありません</p>
                        </c:if>
                    </div>
                </div>

                <!-- クイックアクション -->
                <div class="quick-actions">
                    <h3>クイックアクション</h3>
                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/premium?action=simulate" class="btn btn-primary">保険料見積もり</a>
                        <a href="${pageContext.request.contextPath}/document?action=request" class="btn btn-secondary">資料請求</a>
                        <a href="${pageContext.request.contextPath}/mypage?action=profile" class="btn btn-info">プロフィール編集</a>
                        <a href="${pageContext.request.contextPath}/mypage?action=settings" class="btn btn-warning">設定変更</a>
                    </div>
                </div>
            </div>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <script>
        // ダッシュボードの初期化
        document.addEventListener('DOMContentLoaded', function() {
            // アラートを自動で閉じる
            setTimeout(function() {
                const alerts = document.querySelectorAll('.alert');
                alerts.forEach(function(alert) {
                    alert.style.display = 'none';
                });
            }, 5000);
            
            // アクティビティのアニメーション
            const activityItems = document.querySelectorAll('.activity-item');
            activityItems.forEach(function(item, index) {
                item.style.animationDelay = (index * 0.1) + 's';
            });
        });
    </script>
</body>
</html>