<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>保険料シミュレーション - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/premium.css">
    <script src="${pageContext.request.contextPath}/js/premium-calculator.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>保険料シミュレーション</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/customer?action=list">顧客管理</a>
                <a href="${pageContext.request.contextPath}/premium?action=rates&productId=1">料率表</a>
                <a href="${pageContext.request.contextPath}/">ホーム</a>
            </nav>
        </header>

        <main>
            <!-- メッセージ表示 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    ${errorMessage}
                </div>
            </c:if>

            <div class="form-container">
                <h2>保険料計算</h2>
                
                <form method="post" action="${pageContext.request.contextPath}/premium" id="premiumForm">
                    <input type="hidden" name="action" value="calculate">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="productId">商品選択:</label>
                            <select id="productId" name="productId" class="form-control" required>
                                <option value="1" ${param.productId == '1' or empty param.productId ? 'selected' : ''}>学資保険プランA</option>
                                <option value="2" ${param.productId == '2' ? 'selected' : ''}>学資保険プランB</option>
                                <option value="3" ${param.productId == '3' ? 'selected' : ''}>医療保険プランA</option>
                                <option value="4" ${param.productId == '4' ? 'selected' : ''}>終身保険プランA</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="gender">性別:</label>
                            <select id="gender" name="gender" class="form-control" required>
                                <option value="M" ${param.gender == 'M' or empty param.gender ? 'selected' : ''}>男性</option>
                                <option value="F" ${param.gender == 'F' ? 'selected' : ''}>女性</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="entryAge">加入年齢:</label>
                            <input type="number" id="entryAge" name="entryAge" class="form-control" 
                                   min="0" max="100" value="${not empty param.entryAge ? param.entryAge : '30'}" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="insurancePeriod">保険期間 (年):</label>
                            <input type="number" id="insurancePeriod" name="insurancePeriod" class="form-control" 
                                   min="1" max="50" value="${not empty param.insurancePeriod ? param.insurancePeriod : '20'}" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="insuredAmount">保険金額 (円):</label>
                        <input type="number" id="insuredAmount" name="insuredAmount" class="form-control" 
                               min="100000" step="100000" value="${not empty param.insuredAmount ? param.insuredAmount : '3000000'}" required>
                    </div>

                    <!-- リアルタイム計算結果表示エリア -->
                    <div id="realtime-result" class="realtime-result">
                        <p>入力内容に応じて概算保険料が表示されます</p>
                    </div>

                    <div class="form-group text-right">
                        <button type="submit" class="btn btn-primary">計算実行</button>
                        <a href="${pageContext.request.contextPath}/premium?action=simulate" class="btn btn-secondary">クリア</a>
                    </div>
                </form>
            </div>

            <!-- バッチ計算フォーム -->
            <div class="form-container mt-4">
                <h3>バッチ計算</h3>
                
                <form method="post" action="${pageContext.request.contextPath}/premium" id="batchForm">
                    <input type="hidden" name="action" value="batch">
                    
                    <div class="form-row">
                        <div class="form-group">
                            <label for="batchProductId">商品選択:</label>
                            <select id="batchProductId" name="productId" class="form-control" required>
                                <option value="1">学資保険プランA</option>
                                <option value="2">学資保険プランB</option>
                                <option value="3">医療保険プランA</option>
                                <option value="4">終身保険プランA</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="batchGender">性別:</label>
                            <select id="batchGender" name="gender" class="form-control" required>
                                <option value="M">男性</option>
                                <option value="F">女性</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="ageRange">年齢範囲 (例: 25-35):</label>
                            <input type="text" id="ageRange" name="ageRange" class="form-control" 
                                   placeholder="25-35" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="periodRange">保険期間範囲 (例: 10-20):</label>
                            <input type="text" id="periodRange" name="periodRange" class="form-control" 
                                   placeholder="10-20" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="batchInsuredAmount">保険金額 (円):</label>
                        <input type="number" id="batchInsuredAmount" name="insuredAmount" class="form-control" 
                               min="100000" step="100000" value="3000000" required>
                    </div>

                    <div class="form-group text-right">
                        <button type="submit" class="btn btn-info">バッチ計算実行</button>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <script>
        // 入力値のバリデーション
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', function(e) {
                const insuredAmount = form.querySelector('input[name="insuredAmount"]');
                if (insuredAmount && insuredAmount.value < 100000) {
                    e.preventDefault();
                    alert('保険金額は100,000円以上を指定してください');
                    return false;
                }
                
                // バッチ計算の範囲チェック
                const ageRange = form.querySelector('input[name="ageRange"]');
                const periodRange = form.querySelector('input[name="periodRange"]');
                
                if (ageRange && periodRange) {
                    const agePattern = /^\d+-\d+$/;
                    const periodPattern = /^\d+-\d+$/;
                    
                    if (!agePattern.test(ageRange.value) || !periodPattern.test(periodRange.value)) {
                        e.preventDefault();
                        alert('年齢範囲と保険期間範囲は「数字-数字」の形式で入力してください');
                        return false;
                    }
                }
            });
        });
    </script>
</body>
</html>