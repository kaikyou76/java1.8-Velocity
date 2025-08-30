<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理者ダッシュボード - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>管理者ダッシュボード</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/admin?action=dashboard" class="${activeTab == 'dashboard' ? 'active' : ''}">ダッシュボード</a>
                <a href="${pageContext.request.contextPath}/admin?action=customers" class="${activeTab == 'customers' ? 'active' : ''}">顧客管理</a>
                <a href="${pageContext.request.contextPath}/admin?action=requests" class="${activeTab == 'requests' ? 'active' : ''}">資料請求</a>
                <a href="${pageContext.request.contextPath}/admin?action=reports" class="${activeTab == 'reports' ? 'active' : ''}">レポート</a>
                <a href="${pageContext.request.contextPath}/admin?action=users" class="${activeTab == 'users' ? 'active' : ''}">ユーザー管理</a>
                <a href="${pageContext.request.contextPath}/admin?action=settings" class="${activeTab == 'settings' ? 'active' : ''}">システム設定</a>
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

            <div class="admin-content">
                <div class="welcome-section">
                    <h2>システム概要</h2>
                    <p>最終更新: <fmt:formatDate value="<%= new java.util.Date() %>" pattern="yyyy年MM月dd日 HH:mm" /></p>
                </div>

                <div class="stats-grid">
                    <!-- 顧客統計 -->
                    <div class="stat-card">
                        <div class="stat-icon">👥</div>
                        <div class="stat-content">
                            <h3>総顧客数</h3>
                            <div class="stat-number">${totalCustomers}</div>
                            <p>登録済み顧客</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin?action=customers" class="stat-link">詳細</a>
                    </div>

                    <!-- 資料請求統計 -->
                    <div class="stat-card">
                        <div class="stat-icon">📋</div>
                        <div class="stat-content">
                            <h3>総資料請求数</h3>
                            <div class="stat-number">${totalRequests}</div>
                            <p>累計リクエスト</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin?action=requests" class="stat-link">詳細</a>
                    </div>

                    <!-- 受付中リクエスト -->
                    <div class="stat-card">
                        <div class="stat-icon">⏳</div>
                        <div class="stat-content">
                            <h3>処理待ち</h3>
                            <div class="stat-number">${requestStats['受付'] != null ? requestStats['受付'] : 0}</div>
                            <p>受付中リクエスト</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin?action=requests&status=受付" class="stat-link">処理</a>
                    </div>

                    <!-- 完了リクエスト -->
                    <div class="stat-card">
                        <div class="stat-icon">✅</div>
                        <div class="stat-content">
                            <h3>完了</h3>
                            <div class="stat-number">${requestStats['完了'] != null ? requestStats['完了'] : 0}</div>
                            <p>処理済みリクエスト</p>
                        </div>
                        <a href="${pageContext.request.contextPath}/admin?action=requests&status=完了" class="stat-link">確認</a>
                    </div>
                </div>

                <!-- ステータス分布 -->
                <div class="status-distribution">
                    <h3>資料請求ステータス分布</h3>
                    <div class="distribution-grid">
                        <div class="distribution-item">
                            <span class="status-badge status-received">受付</span>
                            <div class="distribution-bar">
                                <div class="distribution-fill" style="width: ${(requestStats['受付'] != null ? requestStats['受付'] : 0) / totalRequests * 100}%"></div>
                            </div>
                            <span class="distribution-count">${requestStats['受付'] != null ? requestStats['受付'] : 0}</span>
                        </div>
                        <div class="distribution-item">
                            <span class="status-badge status-processing">処理中</span>
                            <div class="distribution-bar">
                                <div class="distribution-fill" style="width: ${(requestStats['処理中'] != null ? requestStats['処理中'] : 0) / totalRequests * 100}%"></div>
                            </div>
                            <span class="distribution-count">${requestStats['処理中'] != null ? requestStats['処理中'] : 0}</span>
                        </div>
                        <div class="distribution-item">
                            <span class="status-badge status-completed">完了</span>
                            <div class="distribution-bar">
                                <div class="distribution-fill" style="width: ${(requestStats['完了'] != null ? requestStats['完了'] : 0) / totalRequests * 100}%"></div>
                            </div>
                            <span class="distribution-count">${requestStats['完了'] != null ? requestStats['完了'] : 0}</span>
                        </div>
                        <div class="distribution-item">
                            <span class="status-badge status-cancelled">取消</span>
                            <div class="distribution-bar">
                                <div class="distribution-fill" style="width: ${(requestStats['取消'] != null ? requestStats['取消'] : 0) / totalRequests * 100}%"></div>
                            </div>
                            <span class="distribution-count">${requestStats['取消'] != null ? requestStats['取消'] : 0}</span>
                        </div>
                    </div>
                </div>

                <!-- 最近の活動 -->
                <div class="recent-activities">
                    <h3>最近の活動</h3>
                    <div class="activity-list">
                        <div class="activity-item">
                            <span class="activity-time">10:30</span>
                            <span class="activity-type">新規顧客登録</span>
                            <span class="activity-details">山田太郎さんが新規登録</span>
                        </div>
                        <div class="activity-item">
                            <span class="activity-time">09:45</span>
                            <span class="activity-type">資料請求</span>
                            <span class="activity-details">鈴木花子さんが学資保険の資料を請求</span>
                        </div>
                        <div class="activity-item">
                            <span class="activity-time">昨日</span>
                            <span class="activity-type">ステータス更新</span>
                            <span class="activity-details">15件のリクエストを完了に更新</span>
                        </div>
                    </div>
                </div>

                <!-- クイックアクション -->
                <div class="quick-actions">
                    <h3>クイックアクション</h3>
                    <div class="action-grid">
                        <a href="${pageContext.request.contextPath}/admin?action=reports" class="action-card">
                            <div class="action-icon">📊</div>
                            <h4>レポート生成</h4>
                            <p>営業レポートを生成</p>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin?action=users" class="action-card">
                            <div class="action-icon">👤</div>
                            <h4>ユーザー管理</h4>
                            <p>ユーザー権限を設定</p>
                        </a>
                        <a href="${pageContext.request.contextPath}/admin?action=settings" class="action-card">
                            <div class="action-icon">⚙️</div>
                            <h4>システム設定</h4>
                            <p>システム設定を変更</p>
                        </a>
                        <a href="${pageContext.request.contextPath}/document?action=request" class="action-card">
                            <div class="action-icon">➕</div>
                            <h4>新規請求</h4>
                            <p>資料請求を登録</p>
                        </a>
                    </div>
                </div>
            </div>
        </main>

        <footer>
            <p>&copy; 2024 保険システム - 管理者画面</p>
        </footer>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            // アラートを自動で閉じる
            setTimeout(function() {
                const alerts = document.querySelectorAll('.alert');
                alerts.forEach(function(alert) {
                    alert.style.display = 'none';
                });
            }, 5000);
            
            // 統計カードのアニメーション
            const statCards = document.querySelectorAll('.stat-card');
            statCards.forEach(function(card, index) {
                card.style.animationDelay = (index * 0.1) + 's';
            });
        });
    </script>
</body>
</html>