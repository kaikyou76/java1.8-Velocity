<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>APIドキュメント - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/api-documentation.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/themes/prism-tomorrow.min.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/sidebar.jsp" />
        
        <div class="main-content">
            <div class="header">
                <h1><i class="fas fa-code"></i> APIドキュメント</h1>
                <div class="header-actions">
                    <button class="btn btn-primary" onclick="toggleTestMode()">
                        <i class="fas fa-flask"></i> テストモード
                    </button>
                </div>
            </div>
            
            <!-- API概要 -->
            <div class="api-overview">
                <div class="card">
                    <div class="card-header">
                        <h2><i class="fas fa-info-circle"></i> API概要</h2>
                    </div>
                    <div class="card-body">
                        <div class="info-grid">
                            <div class="info-item">
                                <h4>ベースURL</h4>
                                <code>${pageContext.request.contextPath}/api</code>
                            </div>
                            <div class="info-item">
                                <h4>認証方式</h4>
                                <span>セッションベース認証</span>
                            </div>
                            <div class="info-item">
                                <h4>データ形式</h4>
                                <span>JSON</span>
                            </div>
                            <div class="info-item">
                                <h4>HTTPメソッド</h4>
                                <span>GET, POST, PUT, DELETE</span>
                            </div>
                        </div>
                        
                        <div class="response-format">
                            <h4>レスポンス形式</h4>
                            <pre><code class="language-json">{
  "status": 200,
  "message": "success",
  "data": {},
  "meta": {}
}</code></pre>
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- APIエンドポイント一覧 -->
            <div class="api-endpoints">
                <div class="endpoint-section" id="customer-api">
                    <div class="endpoint-header">
                        <h3><i class="fas fa-users"></i> 顧客管理API</h3>
                        <button class="btn btn-sm btn-primary" onclick="expandEndpoint('customer-api')">
                            <i class="fas fa-expand"></i>
                        </button>
                    </div>
                    
                    <div class="endpoint-content" id="customer-api-content">
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-get">GET</div>
                                <div class="endpoint-url">/api/customers</div>
                                <div class="endpoint-description">顧客リストを取得</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="parameters">
                                    <h5>クエリパラメータ</h5>
                                    <table class="parameter-table">
                                        <tr>
                                            <th>パラメータ</th>
                                            <th>型</th>
                                            <th>必須</th>
                                            <th>説明</th>
                                        </tr>
                                        <tr>
                                            <td>page</td>
                                            <td>int</td>
                                            <td>いいえ</td>
                                            <td>ページ番号 (デフォルト: 1)</td>
                                        </tr>
                                        <tr>
                                            <td>size</td>
                                            <td>int</td>
                                            <td>いいえ</td>
                                            <td>ページサイズ (デフォルト: 20)</td>
                                        </tr>
                                        <tr>
                                            <td>sortBy</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>ソート項目 (デフォルト: id)</td>
                                        </tr>
                                        <tr>
                                            <td>sortOrder</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>ソート順 (デフォルト: asc)</td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div class="example">
                                    <h5>リクエスト例</h5>
                                    <pre><code class="language-bash">GET /api/customers?page=1&size=10&sortBy=name&sortOrder=asc</code></pre>
                                    
                                    <h5>レスポンス例</h5>
                                    <pre><code class="language-json">{
  "status": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "山田太郎",
      "email": "yamada@example.com",
      "phone": "03-1234-5678",
      "address": "東京都渋谷区...",
      "status": "active",
      "createdAt": "2024-01-01T10:00:00"
    }
  ],
  "meta": {
    "page": 1,
    "size": 10,
    "total": 100,
    "pages": 10
  }
}</code></pre>
                                </div>
                                
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('GET', '/api/customers', null)">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-GET-api-customers"></div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-post">POST</div>
                                <div class="endpoint-url">/api/customers</div>
                                <div class="endpoint-description">新規顧客を作成</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="parameters">
                                    <h5>リクエストボディ</h5>
                                    <table class="parameter-table">
                                        <tr>
                                            <th>パラメータ</th>
                                            <th>型</th>
                                            <th>必須</th>
                                            <th>説明</th>
                                        </tr>
                                        <tr>
                                            <td>name</td>
                                            <td>string</td>
                                            <td>はい</td>
                                            <td>顧客名</td>
                                        </tr>
                                        <tr>
                                            <td>email</td>
                                            <td>string</td>
                                            <td>はい</td>
                                            <td>メールアドレス</td>
                                        </tr>
                                        <tr>
                                            <td>phone</td>
                                            <td>string</td>
                                            <td>はい</td>
                                            <td>電話番号</td>
                                        </tr>
                                        <tr>
                                            <td>address</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>住所</td>
                                        </tr>
                                        <tr>
                                            <td>birthDate</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>生年月日 (yyyy-MM-dd)</td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div class="example">
                                    <h5>リクエスト例</h5>
                                    <pre><code class="language-json">{
  "name": "山田太郎",
  "email": "yamada@example.com",
  "phone": "03-1234-5678",
  "address": "東京都渋谷区...",
  "birthDate": "1990-01-01"
}</code></pre>
                                    
                                    <h5>レスポンス例</h5>
                                    <pre><code class="language-json">{
  "status": 201,
  "message": "success",
  "data": {
    "id": 101,
    "name": "山田太郎",
    "email": "yamada@example.com",
    "phone": "03-1234-5678",
    "status": "active",
    "createdAt": "2024-01-01T10:00:00"
  }
}</code></pre>
                                </div>
                                
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('POST', '/api/customers', getCustomerTestData())">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-POST-api-customers"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 契約管理API -->
                <div class="endpoint-section" id="contract-api">
                    <div class="endpoint-header">
                        <h3><i class="fas fa-file-contract"></i> 契約管理API</h3>
                        <button class="btn btn-sm btn-primary" onclick="expandEndpoint('contract-api')">
                            <i class="fas fa-expand"></i>
                        </button>
                    </div>
                    
                    <div class="endpoint-content" id="contract-api-content">
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-get">GET</div>
                                <div class="endpoint-url">/api/contracts</div>
                                <div class="endpoint-description">契約リストを取得</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="parameters">
                                    <h5>クエリパラメータ</h5>
                                    <table class="parameter-table">
                                        <tr>
                                            <th>パラメータ</th>
                                            <th>型</th>
                                            <th>必須</th>
                                            <th>説明</th>
                                        </tr>
                                        <tr>
                                            <td>page</td>
                                            <td>int</td>
                                            <td>いいえ</td>
                                            <td>ページ番号</td>
                                        </tr>
                                        <tr>
                                            <td>status</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>契約ステータスでフィルタ</td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('GET', '/api/contracts', null)">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-GET-api-contracts"></div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-post">POST</div>
                                <div class="endpoint-url">/api/contracts</div>
                                <div class="endpoint-description">新規契約を作成</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('POST', '/api/contracts', getContractTestData())">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-POST-api-contracts"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 料率計算API -->
                <div class="endpoint-section" id="premium-api">
                    <div class="endpoint-header">
                        <h3><i class="fas fa-calculator"></i> 料率計算API</h3>
                        <button class="btn btn-sm btn-primary" onclick="expandEndpoint('premium-api')">
                            <i class="fas fa-expand"></i>
                        </button>
                    </div>
                    
                    <div class="endpoint-content" id="premium-api-content">
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-post">POST</div>
                                <div class="endpoint-url">/api/premium/calculate</div>
                                <div class="endpoint-description">保険料を計算</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="parameters">
                                    <h5>リクエストパラメータ</h5>
                                    <table class="parameter-table">
                                        <tr>
                                            <th>パラメータ</th>
                                            <th>型</th>
                                            <th>必須</th>
                                            <th>説明</th>
                                        </tr>
                                        <tr>
                                            <td>productId</td>
                                            <td>int</td>
                                            <td>はい</td>
                                            <td>商品ID</td>
                                        </tr>
                                        <tr>
                                            <td>age</td>
                                            <td>int</td>
                                            <td>はい</td>
                                            <td>年齢</td>
                                        </tr>
                                        <tr>
                                            <td>coverageAmount</td>
                                            <td>double</td>
                                            <td>はい</td>
                                            <td>保険金額</td>
                                        </tr>
                                        <tr>
                                            <td>gender</td>
                                            <td>string</td>
                                            <td>いいえ</td>
                                            <td>性別 (male/female)</td>
                                        </tr>
                                    </table>
                                </div>
                                
                                <div class="example">
                                    <h5>リクエスト例</h5>
                                    <pre><code class="language-json">{
  "productId": 1,
  "age": 30,
  "coverageAmount": 1000000,
  "gender": "male"
}</code></pre>
                                    
                                    <h5>レスポンス例</h5>
                                    <pre><code class="language-json">{
  "status": 200,
  "message": "success",
  "data": {
    "productId": 1,
    "age": 30,
    "coverageAmount": 1000000,
    "gender": "male",
    "calculatedPremium": 12000,
    "currency": "JPY"
  }
}</code></pre>
                                </div>
                                
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('POST', '/api/premium/calculate', getPremiumTestData())">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-POST-api-premium-calculate"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- 資料請求API -->
                <div class="endpoint-section" id="document-api">
                    <div class="endpoint-header">
                        <h3><i class="fas fa-envelope"></i> 資料請求API</h3>
                        <button class="btn btn-sm btn-primary" onclick="expandEndpoint('document-api')">
                            <i class="fas fa-expand"></i>
                        </button>
                    </div>
                    
                    <div class="endpoint-content" id="document-api-content">
                        <div class="endpoint-item">
                            <div class="endpoint-info">
                                <div class="endpoint-method method-post">POST</div>
                                <div class="endpoint-url">/api/document-requests</div>
                                <div class="endpoint-description">資料請求を作成</div>
                            </div>
                            <div class="endpoint-details">
                                <div class="test-section">
                                    <h5>テスト</h5>
                                    <button class="btn btn-sm btn-primary" onclick="testApi('POST', '/api/document-requests', getDocumentTestData())">
                                        <i class="fas fa-play"></i> テスト実行
                                    </button>
                                    <div class="test-result" id="test-POST-api-document-requests"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <!-- APIテスト結果モーダル -->
    <div id="apiTestModal" class="modal">
        <div class="modal-content modal-lg">
            <div class="modal-header">
                <h3>APIテスト結果</h3>
                <span class="close" onclick="closeTestModal()">&times;</span>
            </div>
            <div class="modal-body">
                <div class="test-info">
                    <div class="test-request">
                        <h4>リクエスト</h4>
                        <div id="testRequestInfo"></div>
                    </div>
                    <div class="test-response">
                        <h4>レスポンス</h4>
                        <div id="testResponseInfo"></div>
                    </div>
                </div>
                <div class="test-result-content">
                    <h4>レスポンスデータ</h4>
                    <pre><code id="testResponseData" class="language-json"></code></pre>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline" onclick="closeTestModal()">閉じる</button>
                <button type="button" class="btn btn-primary" onclick="copyToClipboard()">クリップボードにコピー</button>
            </div>
        </div>
    </div>
    
    <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/prism.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-json.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/prism/1.29.0/components/prism-bash.min.js"></script>
    <script>
        let testMode = false;
        
        function toggleTestMode() {
            testMode = !testMode;
            const testSections = document.querySelectorAll('.test-section');
            testSections.forEach(section => {
                section.style.display = testMode ? 'block' : 'none';
            });
        }
        
        function expandEndpoint(endpointId) {
            const content = document.getElementById(endpointId + '-content');
            const button = event.currentTarget;
            
            if (content.style.display === 'none') {
                content.style.display = 'block';
                button.innerHTML = '<i class="fas fa-compress"></i>';
            } else {
                content.style.display = 'none';
                button.innerHTML = '<i class="fas fa-expand"></i>';
            }
        }
        
        function getCustomerTestData() {
            return {
                name: "テスト顧客",
                email: "test@example.com",
                phone: "03-1234-5678",
                address: "東京都渋谷区...",
                birthDate: "1990-01-01"
            };
        }
        
        function getContractTestData() {
            return {
                customerId: 1,
                productId: 1,
                contractNumber: "TEST-001",
                startDate: "2024-01-01",
                endDate: "2025-01-01",
                premiumAmount: 12000,
                paymentMethod: "bank_transfer",
                status: "active"
            };
        }
        
        function getPremiumTestData() {
            return {
                productId: 1,
                age: 30,
                coverageAmount: 1000000,
                gender: "male",
                paymentMethod: "annual"
            };
        }
        
        function getDocumentTestData() {
            return {
                customerName: "山田太郎",
                email: "yamada@example.com",
                phone: "03-1234-5678",
                address: "東京都渋谷区...",
                requestType: "document",
                details: "商品資料請求",
                preferredContactMethod: "email"
            };
        }
        
        async function testApi(method, url, data) {
            try {
                const fullUrl = '${pageContext.request.contextPath}' + url;
                const testResultDiv = document.getElementById('test-' + method + url.replace(/\//g, '-'));
                
                testResultDiv.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i> テスト実行中...</div>';
                
                const options = {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                };
                
                if (data) {
                    options.body = JSON.stringify(data);
                }
                
                const response = await fetch(fullUrl, options);
                const result = await response.json();
                
                testResultDiv.innerHTML = `
                    <div class="test-result-item ${response.status >= 200 && response.status < 300 ? 'success' : 'error'}">
                        <div class="test-status">ステータス: ${response.status}</div>
                        <div class="test-summary">${result.message}</div>
                        <button class="btn btn-sm btn-primary" onclick="showTestResult('${method}', '${url}', ${JSON.stringify(data).replace(/"/g, '&quot;')}, ${JSON.stringify(result).replace(/"/g, '&quot;')})">
                            <i class="fas fa-eye"></i> 詳細
                        </button>
                    </div>
                `;
                
            } catch (error) {
                testResultDiv.innerHTML = `
                    <div class="test-result-item error">
                        <div class="test-status">エラー</div>
                        <div class="test-summary">${error.message}</div>
                    </div>
                `;
            }
        }
        
        function showTestResult(method, url, requestData, responseData) {
            const requestInfo = document.getElementById('testRequestInfo');
            const responseInfo = document.getElementById('testResponseInfo');
            const responseDataElement = document.getElementById('testResponseData');
            
            requestInfo.innerHTML = `
                <div><strong>メソッド:</strong> ${method}</div>
                <div><strong>URL:</strong> ${url}</div>
                <div><strong>リクエストデータ:</strong></div>
                <pre><code>${JSON.stringify(JSON.parse(requestData.replace(/&quot;/g, '"')), null, 2)}</code></pre>
            `;
            
            const response = JSON.parse(responseData.replace(/&quot;/g, '"'));
            responseInfo.innerHTML = `
                <div><strong>ステータス:</strong> ${response.status}</div>
                <div><strong>メッセージ:</strong> ${response.message}</div>
            `;
            
            responseDataElement.textContent = JSON.stringify(response, null, 2);
            
            // シンタックスハイライト
            Prism.highlightAll();
            
            document.getElementById('apiTestModal').style.display = 'block';
        }
        
        function closeTestModal() {
            document.getElementById('apiTestModal').style.display = 'none';
        }
        
        function copyToClipboard() {
            const responseData = document.getElementById('testResponseData').textContent;
            navigator.clipboard.writeText(responseData).then(() => {
                alert('クリップボードにコピーしました');
            });
        }
        
        // 初期化
        document.addEventListener('DOMContentLoaded', function() {
            // シンタックスハイライト
            Prism.highlightAll();
            
            // テストモードの初期状態
            const testSections = document.querySelectorAll('.test-section');
            testSections.forEach(section => {
                section.style.display = testMode ? 'block' : 'none';
            });
        });
        
        // モーダル外クリックで閉じる
        window.onclick = function(event) {
            const modal = document.getElementById('apiTestModal');
            if (event.target === modal) {
                closeTestModal();
            }
        };
    </script>
</body>
</html>