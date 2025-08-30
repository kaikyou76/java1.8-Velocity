<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${not empty isEdit ? '顧客情報編集' : '新規顧客登録'} - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
    <div class="container">
        <header>
            <h1>${not empty isEdit ? '顧客情報編集' : '新規顧客登録'}</h1>
            <nav>
                <a href="${pageContext.request.contextPath}/customer?action=list">顧客一覧</a>
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
                <form method="post" action="${pageContext.request.contextPath}/customer">
                    <input type="hidden" name="action" value="${not empty isEdit ? 'update' : 'add'}">
                    <c:if test="${not empty isEdit and not empty customer}">
                        <input type="hidden" name="id" value="${customer.id}">
                    </c:if>

                    <h2>基本情報</h2>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="customerCode">顧客コード *</label>
                            <input type="text" id="customerCode" name="customerCode" class="form-control" 
                                   value="${customer.customerCode}" required 
                                   ${not empty isEdit ? 'readonly' : ''}>
                        </div>
                        
                        <div class="form-group">
                            <label for="gender">性別 *</label>
                            <select id="gender" name="gender" class="form-control" required>
                                <option value="" ${empty customer.gender ? 'selected' : ''}>選択してください</option>
                                <option value="M" ${customer.gender == 'M' ? 'selected' : ''}>男性</option>
                                <option value="F" ${customer.gender == 'F' ? 'selected' : ''}>女性</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="lastName">姓 *</label>
                            <input type="text" id="lastName" name="lastName" class="form-control" 
                                   value="${customer.lastName}" required>
                        </div>
                        
                        <div class="form-group">
                            <label for="firstName">名 *</label>
                            <input type="text" id="firstName" name="firstName" class="form-control" 
                                   value="${customer.firstName}" required>
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="lastNameKana">姓（カナ）</label>
                            <input type="text" id="lastNameKana" name="lastNameKana" class="form-control" 
                                   value="${customer.lastNameKana}" pattern="[ァ-ヴー・]*" 
                                   title="カタカナで入力してください">
                        </div>
                        
                        <div class="form-group">
                            <label for="firstNameKana">名（カナ）</label>
                            <input type="text" id="firstNameKana" name="firstNameKana" class="form-control" 
                                   value="${customer.firstNameKana}" pattern="[ァ-ヴー・]*" 
                                   title="カタカナで入力してください">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="birthDate">生年月日 *</label>
                            <input type="date" id="birthDate" name="birthDate" class="form-control" 
                                   value="<fmt:formatDate value='${customer.birthDate}' pattern='yyyy-MM-dd' />" 
                                   required>
                        </div>
                        
                        <div class="form-group">
                            <label for="age">年齢</label>
                            <input type="number" id="age" name="age" class="form-control" 
                                   value="${customer.age}" readonly>
                        </div>
                    </div>

                    <h2>連絡先情報</h2>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="postalCode">郵便番号</label>
                            <input type="text" id="postalCode" name="postalCode" class="form-control" 
                                   value="${customer.postalCode}" pattern="\d{3}-?\d{4}" 
                                   title="123-4567の形式で入力してください">
                        </div>
                        
                        <div class="form-group">
                            <label for="prefecture">都道府県</label>
                            <select id="prefecture" name="prefecture" class="form-control">
                                <option value="" ${empty customer.prefecture ? 'selected' : ''}>選択してください</option>
                                <option value="北海道" ${customer.prefecture == '北海道' ? 'selected' : ''}>北海道</option>
                                <option value="青森県" ${customer.prefecture == '青森県' ? 'selected' : ''}>青森県</option>
                                <option value="東京都" ${customer.prefecture == '東京都' ? 'selected' : ''}>東京都</option>
                                <option value="神奈川県" ${customer.prefecture == '神奈川県' ? 'selected' : ''}>神奈川県</option>
                                <option value="大阪府" ${customer.prefecture == '大阪府' ? 'selected' : ''}>大阪府</option>
                                <!-- 他の都道府県を追加 -->
                            </select>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="city">市区町村</label>
                        <input type="text" id="city" name="city" class="form-control" value="${customer.city}">
                    </div>

                    <div class="form-group">
                        <label for="addressLine1">住所1</label>
                        <input type="text" id="addressLine1" name="addressLine1" class="form-control" 
                               value="${customer.addressLine1}">
                    </div>

                    <div class="form-group">
                        <label for="addressLine2">住所2</label>
                        <input type="text" id="addressLine2" name="addressLine2" class="form-control" 
                               value="${customer.addressLine2}">
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="phoneNumber">電話番号</label>
                            <input type="tel" id="phoneNumber" name="phoneNumber" class="form-control" 
                                   value="${customer.phoneNumber}" pattern="\d{2,4}-?\d{2,4}-?\d{4}" 
                                   title="電話番号の形式で入力してください">
                        </div>
                        
                        <div class="form-group">
                            <label for="email">メールアドレス</label>
                            <input type="email" id="email" name="email" class="form-control" 
                                   value="${customer.email}">
                        </div>
                    </div>

                    <h2>その他情報</h2>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="occupation">職業</label>
                            <input type="text" id="occupation" name="occupation" class="form-control" 
                                   value="${customer.occupation}">
                        </div>
                        
                        <div class="form-group">
                            <label for="annualIncome">年収（円）</label>
                            <input type="number" id="annualIncome" name="annualIncome" class="form-control" 
                                   value="${customer.annualIncome}" min="0" step="100000">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="familyComposition">家族構成</label>
                        <textarea id="familyComposition" name="familyComposition" class="form-control" 
                                  rows="3">${customer.familyComposition}</textarea>
                    </div>

                    <div class="form-group text-right">
                        <button type="submit" class="btn btn-primary">
                            ${not empty isEdit ? '更新' : '登録'}
                        </button>
                        <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-secondary">キャンセル</a>
                    </div>
                </form>
            </div>
        </main>

        <footer>
            <p>&copy; 2024 保険システム</p>
        </footer>
    </div>

    <script>
        // 生年月日から年齢を自動計算
        document.getElementById('birthDate').addEventListener('change', function() {
            const birthDate = new Date(this.value);
            if (!isNaN(birthDate.getTime())) {
                const today = new Date();
                let age = today.getFullYear() - birthDate.getFullYear();
                const monthDiff = today.getMonth() - birthDate.getMonth();
                
                if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                    age--;
                }
                
                document.getElementById('age').value = age;
            }
        });

        // 郵便番号から住所自動入力（サンプル実装）
        document.getElementById('postalCode').addEventListener('blur', function() {
            const postalCode = this.value.replace(/[^0-9]/g, '');
            if (postalCode.length === 7) {
                // ここで住所検索APIを呼び出す実装が可能
                console.log('郵便番号検索:', postalCode);
                // デモ用：東京都の住所を設定
                if (postalCode.startsWith('100')) {
                    document.getElementById('prefecture').value = '東京都';
                    document.getElementById('city').value = '千代田区';
                }
            }
        });

        // フォーム送信前のバリデーション
        document.querySelector('form').addEventListener('submit', function(e) {
            const requiredFields = ['customerCode', 'lastName', 'firstName', 'gender', 'birthDate'];
            let isValid = true;
            
            requiredFields.forEach(field => {
                const input = document.getElementById(field);
                if (!input.value.trim()) {
                    isValid = false;
                    input.style.borderColor = '#dc3545';
                } else {
                    input.style.borderColor = '';
                }
            });
            
            if (!isValid) {
                e.preventDefault();
                alert('必須項目を入力してください');
            }
        });
    </script>
</body>
</html>