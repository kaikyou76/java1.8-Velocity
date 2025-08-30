<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>バッチ処理管理 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>
<body>
    <div class="container">
        <!-- ヘッダー -->
        <header class="header">
            <h1>バッチ処理管理</h1>
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/admin">管理者ダッシュボード</a> &gt; バッチ処理管理
            </nav>
        </header>

        <!-- メッセージ表示 -->
        <c:if test="${not empty message}">
            <div class="alert alert-${messageType}">
                ${message}
            </div>
        </c:if>

        <div class="admin-content">
            <div class="card-grid">
                <!-- 保険料更新バッチ -->
                <div class="card">
                    <div class="card-header">
                        <h3>保険料更新バッチ</h3>
                    </div>
                    <div class="card-body">
                        <p><strong>実行スケジュール:</strong> 毎日 02:00</p>
                        <p><strong>処理内容:</strong></p>
                        <ul>
                            <li>有効期限切れ料率の無効化</li>
                            <li>新しい料率の有効化</li>
                            <li>契約保険料の再計算</li>
                        </ul>
                        <form method="post" action="${pageContext.request.contextPath}/admin/batch" class="mt-3">
                            <input type="hidden" name="action" value="premium_update">
                            <button type="submit" class="btn btn-primary btn-block">手動実行</button>
                        </form>
                    </div>
                </div>

                <!-- 契約ステータス更新バッチ -->
                <div class="card">
                    <div class="card-header">
                        <h3>契約ステータス更新バッチ</h3>
                    </div>
                    <div class="card-body">
                        <p><strong>実行スケジュール:</strong> 毎日 03:00</p>
                        <p><strong>処理内容:</strong></p>
                        <ul>
                            <li>審査期限切れ契約の取消</li>
                            <li>支払い期限切れ契約の失効</li>
                            <li>満期契約の完了処理</li>
                            <li>支払いステータスチェック（毎時30分）</li>
                        </ul>
                        <form method="post" action="${pageContext.request.contextPath}/admin/batch" class="mt-3">
                            <input type="hidden" name="action" value="contract_status">
                            <button type="submit" class="btn btn-primary btn-block">手動実行</button>
                        </form>
                    </div>
                </div>

                <!-- レポート生成バッチ -->
                <div class="card">
                    <div class="card-header">
                        <h3>レポート生成バッチ</h3>
                    </div>
                    <div class="card-body">
                        <p><strong>実行スケジュール:</strong></p>
                        <ul>
                            <li>週次: 毎週月曜日 04:00</li>
                            <li>月次: 毎月1日 05:00</li>
                        </ul>
                        <p><strong>処理内容:</strong></p>
                        <ul>
                            <li>契約統計レポート</li>
                            <li>資料請求統計レポート</li>
                            <li>売上統計レポート</li>
                            <li>月次契約/売上/顧客分析レポート</li>
                        </ul>
                        <form method="post" action="${pageContext.request.contextPath}/admin/batch" class="mt-3">
                            <input type="hidden" name="action" value="report_generation">
                            <button type="submit" class="btn btn-primary btn-block">手動実行</button>
                        </form>
                    </div>
                </div>
            </div>

            <!-- 一括実行 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>一括実行</h3>
                </div>
                <div class="card-body">
                    <p>すべてのバッチ処理を一度に実行します。</p>
                    <form method="post" action="${pageContext.request.contextPath}/admin/batch">
                        <input type="hidden" name="action" value="all_batches">
                        <button type="submit" class="btn btn-warning btn-block">すべてのバッチを実行</button>
                    </form>
                </div>
            </div>

            <!-- バッチ状態確認 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>バッチ状態確認</h3>
                </div>
                <div class="card-body">
                    <p>現在のバッチ処理の状態を確認します。</p>
                    <a href="${pageContext.request.contextPath}/admin/batch?action=status" 
                       class="btn btn-info btn-block">状態確認</a>
                </div>
            </div>
        </div>
    </div>

    <!-- フッター -->
    <footer class="footer">
        <p>&copy; 2025 保険システム - バッチ処理管理</p>
    </footer>
</body>
</html>