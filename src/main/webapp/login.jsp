<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ログイン - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/login.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <div class="login-container">
        <div class="login-box">
            <div class="login-header">
                <h1><i class="fas fa-shield-alt"></i> 保険システム</h1>
                <p>ユーザーログイン</p>
            </div>
            
            <c:if test="${not empty timeout}">
                <div class="alert alert-warning">
                    <i class="fas fa-exclamation-triangle"></i>
                    セッションがタイムアウトしました。再ログインしてください。
                </div>
            </c:if>
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle"></i>
                    ${error}
                </div>
            </c:if>
            
            <form method="post" action="${pageContext.request.contextPath}/login" class="login-form">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                
                <div class="form-group">
                    <label for="username">
                        <i class="fas fa-user"></i> ユーザー名
                    </label>
                    <input type="text" id="username" name="username" 
                           class="form-control" required 
                           value="${username}"
                           placeholder="ユーザー名を入力してください">
                </div>
                
                <div class="form-group">
                    <label for="password">
                        <i class="fas fa-lock"></i> パスワード
                    </label>
                    <div class="password-container">
                        <input type="password" id="password" name="password" 
                               class="form-control" required 
                               placeholder="パスワードを入力してください">
                        <button type="button" class="toggle-password" onclick="togglePassword()">
                            <i class="fas fa-eye" id="password-toggle-icon"></i>
                        </button>
                    </div>
                </div>
                
                <div class="form-group">
                    <button type="submit" class="btn btn-primary btn-block">
                        <i class="fas fa-sign-in-alt"></i> ログイン
                    </button>
                </div>
            </form>
            
            <div class="login-footer">
                <div class="system-info">
                    <p><strong>保険システム管理</strong></p>
                    <p>ユーザー認証システム</p>
                </div>
                
                <div class="security-info">
                    <p><i class="fas fa-shield-alt"></i> セキュアな認証</p>
                    <p><i class="fas fa-lock"></i> データ保護</p>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        function togglePassword() {
            const passwordInput = document.getElementById('password');
            const toggleIcon = document.getElementById('password-toggle-icon');
            
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                toggleIcon.className = 'fas fa-eye-slash';
            } else {
                passwordInput.type = 'password';
                toggleIcon.className = 'fas fa-eye';
            }
        }
        
        // フォーム送信時のバリデーション
        document.querySelector('.login-form').addEventListener('submit', function(e) {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value.trim();
            
            if (!username || !password) {
                e.preventDefault();
                alert('ユーザー名とパスワードを入力してください。');
            }
        });
        
        // キーボードショートカット
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                document.querySelector('.login-form').submit();
            }
        });
    </script>
</body>
</html>