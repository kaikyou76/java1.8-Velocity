<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>システムモニタリング - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <style>
        .monitor-dashboard {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .monitor-card {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            border-left: 4px solid #007bff;
        }
        
        .monitor-card.cpu {
            border-left-color: #28a745;
        }
        
        .monitor-card.memory {
            border-left-color: #ffc107;
        }
        
        .monitor-card.database {
            border-left-color: #dc3545;
        }
        
        .monitor-card.disk {
            border-left-color: #6f42c1;
        }
        
        .stat-value {
            font-size: 2rem;
            font-weight: bold;
            color: #2c3e50;
            margin: 0.5rem 0;
        }
        
        .stat-label {
            color: #6c757d;
            font-size: 0.9rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .progress {
            height: 8px;
            background: #e9ecef;
            border-radius: 4px;
            overflow: hidden;
            margin: 1rem 0;
        }
        
        .progress-bar {
            height: 100%;
            background: linear-gradient(45deg, #007bff, #0056b3);
            transition: width 0.3s ease;
        }
        
        .status-indicator {
            display: inline-block;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            margin-right: 0.5rem;
        }
        
        .status-online {
            background: #28a745;
        }
        
        .status-offline {
            background: #dc3545;
        }
        
        .status-warning {
            background: #ffc107;
        }
        
        .refresh-button {
            background: none;
            border: none;
            color: #007bff;
            cursor: pointer;
            font-size: 1.2rem;
        }
        
        .refresh-button:hover {
            color: #0056b3;
        }
        
        .real-time-data {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 4px;
            font-family: 'Courier New', monospace;
            font-size: 0.9rem;
            max-height: 300px;
            overflow-y: auto;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- ヘッダー -->
        <header class="header">
            <h1>システムモニタリング</h1>
            <nav class="breadcrumb">
                <a href="${pageContext.request.contextPath}/admin">管理者ダッシュボード</a> &gt; システムモニタリング
            </nav>
        </header>

        <!-- メッセージ表示 -->
        <c:if test="${not empty message}">
            <div class="alert alert-${messageType}">
                ${message}
            </div>
        </c:if>

        <div class="admin-content">
            <!-- モニタリングダッシュボード -->
            <div class="monitor-dashboard">
                <!-- CPU使用率 -->
                <div class="monitor-card cpu">
                    <div class="card-header">
                        <h3>CPU使用率</h3>
                        <span class="status-indicator status-online"></span>
                    </div>
                    <div class="stat-value" id="cpuUsage">--</div>
                    <div class="stat-label">システム負荷</div>
                    <div class="progress">
                        <div class="progress-bar" id="cpuProgress" style="width: 0%"></div>
                    </div>
                </div>

                <!-- メモリ使用率 -->
                <div class="monitor-card memory">
                    <div class="card-header">
                        <h3>メモリ使用率</h3>
                        <span class="status-indicator status-online"></span>
                    </div>
                    <div class="stat-value" id="memoryUsage">--</div>
                    <div class="stat-label">ヒープメモリ</div>
                    <div class="progress">
                        <div class="progress-bar" id="memoryProgress" style="width: 0%"></div>
                    </div>
                </div>

                <!-- データベース接続 -->
                <div class="monitor-card database">
                    <div class="card-header">
                        <h3>データベース</h3>
                        <span class="status-indicator status-online" id="dbStatus"></span>
                    </div>
                    <div class="stat-value" id="dbConnections">--</div>
                    <div class="stat-label">アクティブ接続</div>
                    <div class="status-text" id="dbStatusText">チェック中...</div>
                </div>

                <!-- ディスク使用率 -->
                <div class="monitor-card disk">
                    <div class="card-header">
                        <h3>ディスク容量</h3>
                        <span class="status-indicator status-online"></span>
                    </div>
                    <div class="stat-value" id="diskUsage">--</div>
                    <div class="stat-label">空き容量</div>
                    <div class="progress">
                        <div class="progress-bar" id="diskProgress" style="width: 0%"></div>
                    </div>
                </div>
            </div>

            <!-- アクションボタン -->
            <div class="card">
                <div class="card-body text-center">
                    <form method="post" action="${pageContext.request.contextPath}/admin/monitor" style="display: inline;">
                        <input type="hidden" name="action" value="refresh">
                        <button type="submit" class="btn btn-primary">
                            データ更新
                        </button>
                    </form>
                    
                    <form method="post" action="${pageContext.request.contextPath}/admin/monitor" style="display: inline; margin-left: 1rem;">
                        <input type="hidden" name="action" value="report">
                        <button type="submit" class="btn btn-success">
                            レポート出力
                        </button>
                    </form>
                    
                    <a href="${pageContext.request.contextPath}/admin/monitor?action=status" 
                       class="btn btn-info" style="margin-left: 1rem;" target="_blank">
                        詳細ステータス
                    </a>
                </div>
            </div>

            <!-- リアルタイムログ表示 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>リアルタイムログ</h3>
                    <button class="refresh-button" onclick="refreshLogs()">
                        ↻
                    </button>
                </div>
                <div class="card-body">
                    <div class="real-time-data" id="realtimeLogs">
                        ログデータを読み込み中...
                    </div>
                </div>
            </div>

            <!-- システム情報 -->
            <div class="card mt-4">
                <div class="card-header">
                    <h3>システム情報</h3>
                </div>
                <div class="card-body">
                    <div class="system-info">
                        <div class="info-row">
                            <label>Javaバージョン:</label>
                            <span>${javaVersion}</span>
                        </div>
                        <div class="info-row">
                            <label>JVM稼働時間:</label>
                            <span id="jvmUptime">--</span>
                        </div>
                        <div class="info-row">
                            <label>スレッド数:</label>
                            <span id="threadCount">--</span>
                        </div>
                        <div class="info-row">
                            <label>ロード済みクラス数:</label>
                            <span id="loadedClasses">--</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // ページ読み込み時にモニタリングデータを取得
        document.addEventListener('DOMContentLoaded', function() {
            updateMonitoringData();
            loadRealtimeLogs();
            
            // 30秒ごとに自動更新
            setInterval(updateMonitoringData, 30000);
            setInterval(loadRealtimeLogs, 10000);
        });
        
        // モニタリングデータ更新
        function updateMonitoringData() {
            fetch('${pageContext.request.contextPath}/admin/monitor?action=status')
                .then(response => response.text())
                .then(data => {
                    // ここでデータを解析してUIを更新
                    console.log('モニタリングデータ:', data);
                    // 実際の実装ではデータを解析して各要素を更新
                    
                    // ダミーデータで表示（実際の実装ではAPIから取得したデータを使用）
                    updateDummyData();
                })
                .catch(error => {
                    console.error('モニタリングデータの取得に失敗しました:', error);
                });
        }
        
        // リアルタイムログ読み込み
        function loadRealtimeLogs() {
            // ここで最新のログを取得して表示
            // 実際の実装ではサーバーからログデータを取得
            document.getElementById('realtimeLogs').innerHTML = 
                '[' + new Date().toLocaleTimeString() + '] モニタリングデータを更新しました\\n' +
                '[' + new Date().toLocaleTimeString() + '] システムステータス: 正常\\n' +
                '[' + new Date().toLocaleTimeString() + '] メモリ使用率: 45%\\n' +
                '[' + new Date().toLocaleTimeString() + '] CPU負荷: 1.2';
        }
        
        // ログ更新
        function refreshLogs() {
            loadRealtimeLogs();
        }
        
        // ダミーデータ更新（デモ用）
        function updateDummyData() {
            // ランダムな値を生成（実際の実装ではAPIから取得したデータを使用）
            const cpuUsage = Math.floor(Math.random() * 30) + 20;
            const memoryUsage = Math.floor(Math.random() * 40) + 30;
            const diskUsage = Math.floor(Math.random() * 50) + 30;
            
            document.getElementById('cpuUsage').textContent = cpuUsage + '%';
            document.getElementById('cpuProgress').style.width = cpuUsage + '%';
            
            document.getElementById('memoryUsage').textContent = memoryUsage + '%';
            document.getElementById('memoryProgress').style.width = memoryUsage + '%';
            
            document.getElementById('diskUsage').textContent = (100 - diskUsage) + '%';
            document.getElementById('diskProgress').style.width = diskUsage + '%';
            
            document.getElementById('dbConnections').textContent = Math.floor(Math.random() * 10) + 5;
            document.getElementById('dbStatusText').textContent = '正常';
            
            document.getElementById('jvmUptime').textContent = '2時間45分';
            document.getElementById('threadCount').textContent = Math.floor(Math.random() * 50) + 30;
            document.getElementById('loadedClasses').textContent = Math.floor(Math.random() * 5000) + 8000;
        }
        
        // 初期データ表示
        updateDummyData();
    </script>

    <!-- フッター -->
    <footer class="footer">
        <p>&copy; 2025 保険システム - システムモニタリング</p>
    </footer>
</body>
</html>