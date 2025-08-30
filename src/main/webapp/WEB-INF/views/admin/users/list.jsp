<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ユーザー管理 - 保険システム</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/user-management.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <div class="admin-container">
        <jsp:include page="/WEB-INF/views/admin/sidebar.jsp" />
        
        <div class="main-content">
            <div class="header">
                <h1><i class="fas fa-users"></i> ユーザー管理</h1>
                <div class="header-actions">
                    <a href="${pageContext.request.contextPath}/admin/users/create" class="btn btn-primary">
                        <i class="fas fa-plus"></i> 新規ユーザー作成
                    </a>
                </div>
            </div>
            
            <c:if test="${not empty success}">
                <div class="alert alert-success">
                    <i class="fas fa-check-circle"></i> ${success}
                </div>
            </c:if>
            
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-circle"></i> ${error}
                </div>
            </c:if>
            
            <!-- 統計情報 -->
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-icon">
                        <i class="fas fa-users"></i>
                    </div>
                    <div class="stat-content">
                        <h3>${stats.totalUsers}</h3>
                        <p>総ユーザー数</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="stat-icon success">
                        <i class="fas fa-user-check"></i>
                    </div>
                    <div class="stat-content">
                        <h3>${stats.activeUsers}</h3>
                        <p>有効ユーザー</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="stat-icon warning">
                        <i class="fas fa-user-times"></i>
                    </div>
                    <div class="stat-content">
                        <h3>${stats.lockedUsers}</h3>
                        <p>ロック中ユーザー</p>
                    </div>
                </div>
                
                <div class="stat-card">
                    <div class="stat-icon info">
                        <i class="fas fa-user-shield"></i>
                    </div>
                    <div class="stat-content">
                        <h3>${stats.adminCount}</h3>
                        <p>管理者</p>
                    </div>
                </div>
            </div>
            
            <!-- 検索フォーム -->
            <div class="search-container">
                <form method="get" action="${pageContext.request.contextPath}/admin/users/" class="search-form">
                    <div class="search-input-group">
                        <i class="fas fa-search"></i>
                        <input type="text" name="keyword" class="form-control" 
                               placeholder="ユーザー名、メール、氏名、部署で検索..."
                               value="${keyword}">
                        <button type="submit" class="btn btn-secondary">
                            <i class="fas fa-search"></i> 検索
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/users/" class="btn btn-outline">
                            <i class="fas fa-times"></i> クリア
                        </a>
                    </div>
                </form>
            </div>
            
            <!-- ユーザーリスト -->
            <div class="card">
                <div class="card-header">
                    <h2><i class="fas fa-list"></i> ユーザー一覧</h2>
                    <c:if test="${not empty keyword}">
                        <span class="search-result">検索結果: "${keyword}"</span>
                    </c:if>
                </div>
                
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>ユーザー名</th>
                                    <th>氏名</th>
                                    <th>メール</th>
                                    <th>役割</th>
                                    <th>部署</th>
                                    <th>ステータス</th>
                                    <th>最終ログイン</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${users}" var="user">
                                    <tr>
                                        <td>${user.id}</td>
                                        <td>${user.username}</td>
                                        <td>${user.fullName}</td>
                                        <td>${user.email}</td>
                                        <td>
                                            <span class="role-badge role-${user.role.toLowerCase()}">
                                                ${user.roleDisplayName}
                                            </span>
                                        </td>
                                        <td>${user.department}</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${user.accountLocked}">
                                                    <span class="status-badge status-locked">
                                                        <i class="fas fa-lock"></i> ${user.statusDisplayName}
                                                    </span>
                                                </c:when>
                                                <c:when test="${user.active}">
                                                    <span class="status-badge status-active">
                                                        <i class="fas fa-check"></i> ${user.statusDisplayName}
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-inactive">
                                                        <i class="fas fa-times"></i> ${user.statusDisplayName}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${not empty user.lastLogin}">
                                                <fmt:formatDate value="${user.lastLogin}" pattern="yyyy/MM/dd HH:mm"/>
                                            </c:if>
                                            <c:if test="${empty user.lastLogin}">
                                                <span class="text-muted">-</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <div class="action-buttons">
                                                <a href="${pageContext.request.contextPath}/admin/users/view?id=${user.id}" 
                                                   class="btn btn-sm btn-info" title="詳細">
                                                    <i class="fas fa-eye"></i>
                                                </a>
                                                <a href="${pageContext.request.contextPath}/admin/users/edit?id=${user.id}" 
                                                   class="btn btn-sm btn-primary" title="編集">
                                                    <i class="fas fa-edit"></i>
                                                </a>
                                                <c:if test="${user.accountLocked}">
                                                    <button type="button" class="btn btn-sm btn-success" 
                                                            onclick="confirmUnlock(${user.id})" title="ロック解除">
                                                        <i class="fas fa-unlock"></i>
                                                    </button>
                                                </c:if>
                                                <c:if test="${user.active and not user.accountLocked}">
                                                    <button type="button" class="btn btn-sm btn-warning" 
                                                            onclick="confirmToggleStatus(${user.id})" title="無効化">
                                                        <i class="fas fa-ban"></i>
                                                    </button>
                                                </c:if>
                                                <c:if test="${not user.active}">
                                                    <button type="button" class="btn btn-sm btn-success" 
                                                            onclick="confirmToggleStatus(${user.id})" title="有効化">
                                                        <i class="fas fa-check"></i>
                                                    </button>
                                                </c:if>
                                                <button type="button" class="btn btn-sm btn-secondary" 
                                                        onclick="confirmResetPassword(${user.id})" title="パスワードリセット">
                                                    <i class="fas fa-key"></i>
                                                </button>
                                                <c:if test="${user.role != 'ADMIN'}">
                                                    <button type="button" class="btn btn-sm btn-danger" 
                                                            onclick="confirmDelete(${user.id})" title="削除">
                                                        <i class="fas fa-trash"></i>
                                                    </button>
                                                </c:if>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    
                    <c:if test="${empty users}">
                        <div class="no-data">
                            <i class="fas fa-users-slash"></i>
                            <p>ユーザーが見つかりません</p>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
    
    <!-- 確認モーダル -->
    <div id="confirmModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3 id="modalTitle">確認</h3>
                <span class="close" onclick="closeModal()">&times;</span>
            </div>
            <div class="modal-body">
                <p id="modalMessage">実行してよろしいですか？</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline" onclick="closeModal()">キャンセル</button>
                <button type="button" class="btn btn-primary" onclick="confirmAction()">実行</button>
            </div>
        </div>
    </div>
    
    <!-- パスワードリセットモーダル -->
    <div id="passwordModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>パスワードリセット</h3>
                <span class="close" onclick="closePasswordModal()">&times;</span>
            </div>
            <div class="modal-body">
                <form id="passwordResetForm">
                    <input type="hidden" name="csrfToken" value="${csrfToken}">
                    <input type="hidden" name="action" value="reset-password">
                    <input type="hidden" name="id" id="resetUserId">
                    
                    <div class="form-group">
                        <label for="newPassword">新しいパスワード</label>
                        <input type="password" id="newPassword" name="newPassword" 
                               class="form-control" required minlength="8" maxlength="20">
                        <small class="text-muted">
                            パスワードは8文字以上で、大文字・小文字・数字を含める必要があります
                        </small>
                    </div>
                    
                    <div class="form-group">
                        <label for="confirmPassword">パスワード確認</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" 
                               class="form-control" required minlength="8" maxlength="20">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline" onclick="closePasswordModal()">キャンセル</button>
                <button type="button" class="btn btn-primary" onclick="resetPassword()">リセット</button>
            </div>
        </div>
    </div>
    
    <script>
        let currentAction = null;
        let currentUserId = null;
        
        function confirmDelete(userId) {
            currentAction = 'delete';
            currentUserId = userId;
            document.getElementById('modalTitle').textContent = 'ユーザー削除';
            document.getElementById('modalMessage').textContent = 'このユーザーを削除してよろしいですか？この操作は元に戻せません。';
            document.getElementById('confirmModal').style.display = 'block';
        }
        
        function confirmToggleStatus(userId) {
            currentAction = 'toggle-status';
            currentUserId = userId;
            document.getElementById('modalTitle').textContent = 'ステータス変更';
            document.getElementById('modalMessage').textContent = 'このユーザーのステータスを変更してよろしいですか？';
            document.getElementById('confirmModal').style.display = 'block';
        }
        
        function confirmUnlock(userId) {
            currentAction = 'unlock-account';
            currentUserId = userId;
            document.getElementById('modalTitle').textContent = 'アカウントロック解除';
            document.getElementById('modalMessage').textContent = 'このユーザーのアカウントロックを解除してよろしいですか？';
            document.getElementById('confirmModal').style.display = 'block';
        }
        
        function confirmResetPassword(userId) {
            currentUserId = userId;
            document.getElementById('resetUserId').value = userId;
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
            document.getElementById('passwordModal').style.display = 'block';
        }
        
        function confirmAction() {
            const form = document.createElement('form');
            form.method = 'post';
            form.action = '${pageContext.request.contextPath}/admin/users/';
            
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = 'csrfToken';
            csrfInput.value = '${csrfToken}';
            form.appendChild(csrfInput);
            
            const actionInput = document.createElement('input');
            actionInput.type = 'hidden';
            actionInput.name = 'action';
            actionInput.value = currentAction;
            form.appendChild(actionInput);
            
            const idInput = document.createElement('input');
            idInput.type = 'hidden';
            idInput.name = 'id';
            idInput.value = currentUserId;
            form.appendChild(idInput);
            
            document.body.appendChild(form);
            form.submit();
        }
        
        function closeModal() {
            document.getElementById('confirmModal').style.display = 'none';
            currentAction = null;
            currentUserId = null;
        }
        
        function closePasswordModal() {
            document.getElementById('passwordModal').style.display = 'none';
            currentUserId = null;
        }
        
        function resetPassword() {
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (newPassword !== confirmPassword) {
                alert('パスワードが一致しません');
                return;
            }
            
            if (newPassword.length < 8) {
                alert('パスワードは8文字以上である必要があります');
                return;
            }
            
            const form = document.getElementById('passwordResetForm');
            form.action = '${pageContext.request.contextPath}/admin/users/';
            form.submit();
        }
        
        // モーダル外クリックで閉じる
        window.onclick = function(event) {
            const confirmModal = document.getElementById('confirmModal');
            const passwordModal = document.getElementById('passwordModal');
            
            if (event.target === confirmModal) {
                closeModal();
            }
            if (event.target === passwordModal) {
                closePasswordModal();
            }
        }
        
        // Enterキーでモーダル操作
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeModal();
                closePasswordModal();
            }
        });
    </script>
</body>
</html>