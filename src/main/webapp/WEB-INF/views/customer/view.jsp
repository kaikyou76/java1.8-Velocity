<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>顧客詳細 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>顧客詳細情報</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/customer?action=list">顧客一覧</a>
                <a href="${pageContext.request.contextPath}/customer?action=edit&id=${customer.id}">編集</a>
                <a href="${pageContext.request.contextPath}/">ホーム</a>
            </nav>
        </header>

        <main>
            <c:if test="${not empty customer}">
                <div class="customer-details">
                    <!-- 基本情報セクション -->
                    <div class="detail-section">
                        <h2>基本情報</h2>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>顧客コード</label>
                                <span>${customer.customerCode}</span>
                            </div>
                            <div class="detail-item">
                                <label>氏名</label>
                                <span>${customer.lastName} ${customer.firstName}</span>
                            </div>
                            <div class="detail-item">
                                <label>氏名（カナ）</label>
                                <span>${customer.lastNameKana} ${customer.firstNameKana}</span>
                            </div>
                            <div class="detail-item">
                                <label>性別</label>
                                <span>${customer.gender == 'M' ? '男性' : '女性'}</span>
                            </div>
                            <div class="detail-item">
                                <label>生年月日</label>
                                <span>
                                    <fmt:formatDate value="${customer.birthDate}" pattern="yyyy年MM月dd日" />
                                    (${customer.age}歳)
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- 連絡先情報セクション -->
                    <div class="detail-section">
                        <h2>連絡先情報</h2>
                        <div class="detail-grid">
                            <div class="detail-item full-width">
                                <label>郵便番号</label>
                                <span>${customer.postalCode}</span>
                            </div>
                            <div class="detail-item">
                                <label>都道府県</label>
                                <span>${customer.prefecture}</span>
                            </div>
                            <div class="detail-item">
                                <label>市区町村</label>
                                <span>${customer.city}</span>
                            </div>
                            <div class="detail-item full-width">
                                <label>住所1</label>
                                <span>${customer.addressLine1}</span>
                            </div>
                            <div class="detail-item full-width">
                                <label>住所2</label>
                                <span>${customer.addressLine2}</span>
                            </div>
                            <div class="detail-item">
                                <label>電話番号</label>
                                <span>${customer.phoneNumber}</span>
                            </div>
                            <div class="detail-item">
                                <label>メールアドレス</label>
                                <span>${customer.email}</span>
                            </div>
                        </div>
                    </div>

                    <!-- その他情報セクション -->
                    <div class="detail-section">
                        <h2>その他情報</h2>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>職業</label>
                                <span>${customer.occupation}</span>
                            </div>
                            <div class="detail-item">
                                <label>年収</label>
                                <span>
                                    <c:if test="${not empty customer.annualIncome}">
                                        <fmt:formatNumber value="${customer.annualIncome}" pattern="¥#,###" />
                                    </c:if>
                                </span>
                            </div>
                            <div class="detail-item full-width">
                                <label>家族構成</label>
                                <span>${customer.familyComposition}</span>
                            </div>
                        </div>
                    </div>

                    <!-- システム情報セクション -->
                    <div class="detail-section">
                        <h2>システム情報</h2>
                        <div class="detail-grid">
                            <div class="detail-item">
                                <label>登録日時</label>
                                <span>
                                    <fmt:formatDate value="${customer.createdAt}" pattern="yyyy年MM月dd日 HH:mm" />
                                </span>
                            </div>
                            <div class="detail-item">
                                <label>最終更新</label>
                                <span>
                                    <fmt:formatDate value="${customer.updatedAt}" pattern="yyyy年MM月dd日 HH:mm" />
                                </span>
                            </div>
                            <div class="detail-item">
                                <label>ステータス</label>
                                <span class="status ${customer.deletedFlag ? 'inactive' : 'active'}">
                                    ${customer.deletedFlag ? '無効' : '有効'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- アクションボタン -->
                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/customer?action=edit&id=${customer.id}" 
                           class="btn btn-warning">編集</a>
                        <c:if test="${not customer.deletedFlag}">
                            <a href="${pageContext.request.contextPath}/customer?action=delete&id=${customer.id}" 
                               class="btn btn-danger" 
                               onclick="return confirm('本当にこの顧客を削除しますか？')">削除</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/premium?action=simulate" 
                           class="btn btn-info">保険料見積</a>
                        <a href="${pageContext.request.contextPath}/customer?action=list" 
                           class="btn btn-secondary">一覧に戻る</a>
                    </div>
                </div>
            </c:if>

            <c:if test="${empty customer}">
                <div class="alert alert-error">
                    <p>指定された顧客が見つかりませんでした。</p>
                </div>
                <div class="text-center">
                    <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-primary">顧客一覧に戻る</a>
                </div>
            </c:if>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <style>
        .customer-details {
            max-width: 800px;
            margin: 0 auto;
        }
        
        .detail-section {
            background-color: white;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 2rem;
        }
        
        .detail-section h2 {
            color: #495057;
            margin-bottom: 1rem;
            padding-bottom: 0.5rem;
            border-bottom: 2px solid #007bff;
        }
        
        .detail-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
        }
        
        .detail-item {
            display: flex;
            flex-direction: column;
            padding: 0.75rem;
            background-color: #f8f9fa;
            border-radius: 4px;
        }
        
        .detail-item.full-width {
            grid-column: 1 / -1;
        }
        
        .detail-item label {
            font-weight: 600;
            color: #495057;
            margin-bottom: 0.25rem;
            font-size: 0.9rem;
        }
        
        .detail-item span {
            color: #6c757d;
            word-break: break-word;
        }
        
        .status {
            display: inline-block;
            padding: 0.25rem 0.5rem;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: 600;
        }
        
        .status.active {
            background-color: #d4edda;
            color: #155724;
        }
        
        .status.inactive {
            background-color: #f8d7da;
            color: #721c24;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
            flex-wrap: wrap;
        }
        
        @media (max-width: 768px) {
            .detail-grid {
                grid-template-columns: 1fr;
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .action-buttons .btn {
                width: 100%;
                text-align: center;
            }
        }
    </style>
</body>
</html>