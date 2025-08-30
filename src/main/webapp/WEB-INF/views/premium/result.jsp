<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>保険料計算結果 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>保険料計算結果</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/premium?action=simulate">再計算</a>
                <a href="${pageContext.request.contextPath}/premium?action=rates&productId=${param.productId}">料率表</a>
                <a href="${pageContext.request.contextPath}/">ホーム</a>
            </nav>
        </header>

        <main>
            <c:if test="${not empty calculationResult and calculationResult.success}">
                <div class="result-container">
                    <h2>計算結果</h2>
                    
                    <div class="result-summary">
                        <div class="summary-card">
                            <h3>月々保険料</h3>
                            <p class="premium-amount">
                                <fmt:formatNumber value="${calculationResult.monthlyPremium}" 
                                                  pattern="¥#,###" />
                            </p>
                        </div>
                        
                        <div class="summary-card">
                            <h3>年間保険料</h3>
                            <p class="premium-amount">
                                <fmt:formatNumber value="${calculationResult.annualPremium}" 
                                                  pattern="¥#,###" />
                            </p>
                        </div>
                    </div>

                    <div class="result-details">
                        <h3>計算詳細</h3>
                        <table class="details-table">
                            <tr>
                                <th>保険金額</th>
                                <td>
                                    <fmt:formatNumber value="${calculationResult.insuredAmount}" 
                                                      pattern="¥#,###" />
                                </td>
                            </tr>
                            <tr>
                                <th>基本料率</th>
                                <td>
                                    <fmt:formatNumber value="${calculationResult.baseRate * 100}" 
                                                      pattern="0.0000" />%
                                </td>
                            </tr>
                            <tr>
                                <th>付加料率</th>
                                <td>
                                    <fmt:formatNumber value="${calculationResult.loadingRate * 100}" 
                                                      pattern="0.0000" />%
                                </td>
                            </tr>
                            <tr>
                                <th>総料率</th>
                                <td>
                                    <fmt:formatNumber value="${calculationResult.totalRate * 100}" 
                                                      pattern="0.0000" />%
                                </td>
                            </tr>
                            <tr>
                                <th>商品名</th>
                                <td>${calculationResult.premiumRate.productName}</td>
                            </tr>
                            <tr>
                                <th>性別</th>
                                <td>${param.gender == 'M' ? '男性' : '女性'}</td>
                            </tr>
                            <tr>
                                <th>加入年齢</th>
                                <td>${param.entryAge}歳</td>
                            </tr>
                            <tr>
                                <th>保険期間</th>
                                <td>${param.insurancePeriod}年</td>
                            </tr>
                            <tr>
                                <th>計算日時</th>
                                <td>
                                    <fmt:formatDate value="${calculationResult.calculationDate}" 
                                                   pattern="yyyy年MM月dd日 HH:mm:ss" />
                                </td>
                            </tr>
                        </table>
                    </div>

                    <div class="action-buttons">
                        <a href="${pageContext.request.contextPath}/premium?action=simulate&productId=${param.productId}&gender=${param.gender}&entryAge=${param.entryAge}&insurancePeriod=${param.insurancePeriod}&insuredAmount=${param.insuredAmount}" 
                           class="btn btn-primary">条件変更</a>
                        <button onclick="window.print()" class="btn btn-secondary">印刷</button>
                        <a href="${pageContext.request.contextPath}/document?action=request&productId=${param.productId}" 
                           class="btn btn-info">資料請求</a>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty calculationResult and calculationResult.error}">
                <div class="alert alert-error">
                    <h3>計算エラー</h3>
                    <p>${calculationResult.error}</p>
                </div>
                
                <div class="text-center">
                    <a href="${pageContext.request.contextPath}/premium?action=simulate" 
                       class="btn btn-primary">再計算</a>
                </div>
            </c:if>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <style>
        .result-summary {
            display: flex;
            gap: 2rem;
            margin-bottom: 2rem;
            justify-content: center;
        }
        
        .summary-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            border-radius: 12px;
            text-align: center;
            min-width: 200px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
        }
        
        .summary-card h3 {
            margin: 0 0 1rem 0;
            font-size: 1.2rem;
            font-weight: 600;
        }
        
        .premium-amount {
            font-size: 2rem;
            font-weight: bold;
            margin: 0;
        }
        
        .result-details {
            background-color: #f8f9fa;
            padding: 2rem;
            border-radius: 8px;
            margin-bottom: 2rem;
        }
        
        .details-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .details-table th,
        .details-table td {
            padding: 0.75rem;
            border-bottom: 1px solid #dee2e6;
            text-align: left;
        }
        
        .details-table th {
            width: 120px;
            font-weight: 600;
            color: #495057;
        }
        
        .action-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
        }
        
        @media (max-width: 768px) {
            .result-summary {
                flex-direction: column;
                gap: 1rem;
            }
            
            .summary-card {
                min-width: auto;
            }
            
            .premium-amount {
                font-size: 1.5rem;
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .details-table th,
            .details-table td {
                padding: 0.5rem;
            }
        }
    </style>
</body>
</html>