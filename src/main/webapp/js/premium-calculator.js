/**
 * 保険料計算JavaScriptライブラリ
 * フロントエンドでの簡易計算とバリデーションを提供
 */

class PremiumCalculator {
    
    constructor() {
        this.baseRates = {
            '1': { // 学資保険プランA
                'M': {
                    '0': 0.0012, '1': 0.00115, '2': 0.0011, '3': 0.00105, '4': 0.0010, '5': 0.00095
                },
                'F': {
                    '0': 0.0011, '1': 0.00105, '2': 0.0010, '3': 0.00095, '4': 0.0009, '5': 0.00085
                }
            },
            '2': { // 学資保険プランB
                'M': {
                    '0': 0.0010, '1': 0.00095, '2': 0.0009, '3': 0.00085, '4': 0.0008, '5': 0.00075
                },
                'F': {
                    '0': 0.0009, '1': 0.00085, '2': 0.0008, '3': 0.00075, '4': 0.0007, '5': 0.00065
                }
            }
        };
        
        this.loadingRates = {
            '1': 0.0001, // 学資保険プランA
            '2': 0.00008 // 学資保険プランB
        };
    }
    
    /**
     * 簡易保険料計算（フロントエンド用）
     */
    calculateSimplePremium(productId, gender, entryAge, insurancePeriod, insuredAmount) {
        // 入力値のバリデーション
        const validation = this.validateInputs(productId, gender, entryAge, insurancePeriod, insuredAmount);
        if (!validation.isValid) {
            return {
                success: false,
                error: validation.message
            };
        }
        
        try {
            // 基本料率の取得
            const baseRate = this.getBaseRate(productId, gender, entryAge);
            if (baseRate === null) {
                return {
                    success: false,
                    error: '指定された条件の料率が見つかりません'
                };
            }
            
            // 付加料率の取得
            const loadingRate = this.loadingRates[productId] || 0.0001;
            
            // 総料率の計算
            const totalRate = baseRate + loadingRate;
            
            // 保険料の計算
            const annualPremium = insuredAmount * totalRate;
            const monthlyPremium = annualPremium / 12;
            
            return {
                success: true,
                annualPremium: annualPremium,
                monthlyPremium: monthlyPremium,
                baseRate: baseRate,
                loadingRate: loadingRate,
                totalRate: totalRate,
                insuredAmount: insuredAmount
            };
            
        } catch (error) {
            return {
                success: false,
                error: '計算中にエラーが発生しました: ' + error.message
            };
        }
    }
    
    /**
     * 基本料率の取得
     */
    getBaseRate(productId, gender, entryAge) {
        const productRates = this.baseRates[productId];
        if (!productRates) {
            return null;
        }
        
        const genderRates = productRates[gender];
        if (!genderRates) {
            return null;
        }
        
        // 年齢に基づく料率の取得
        const ageKey = Math.min(entryAge, 5).toString(); // サンプルデータは0-5歳まで
        return genderRates[ageKey] || null;
    }
    
    /**
     * 入力値のバリデーション
     */
    validateInputs(productId, gender, entryAge, insurancePeriod, insuredAmount) {
        if (!productId || !this.baseRates[productId]) {
            return {
                isValid: false,
                message: '商品を選択してください'
            };
        }
        
        if (!gender || (gender !== 'M' && gender !== 'F')) {
            return {
                isValid: false,
                message: '性別を正しく選択してください'
            };
        }
        
        if (entryAge < 0 || entryAge > 100) {
            return {
                isValid: false,
                message: '加入年齢は0から100の間で指定してください'
            };
        }
        
        if (insurancePeriod <= 0 || insurancePeriod > 50) {
            return {
                isValid: false,
                message: '保険期間は1から50年の間で指定してください'
            };
        }
        
        if (insuredAmount <= 0 || insuredAmount > 1000000000) {
            return {
                isValid: false,
                message: '保険金額は0より大きく、10億円以下で指定してください'
            };
        }
        
        return {
            isValid: true,
            message: '入力値は有効です'
        };
    }
    
    /**
     * 保険金額のフォーマット
     */
    formatCurrency(amount) {
        return new Intl.NumberFormat('ja-JP', {
            style: 'currency',
            currency: 'JPY',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    }
    
    /**
     * 料率のフォーマット
     */
    formatRate(rate) {
        return (rate * 100).toFixed(4) + '%';
    }
    
    /**
     * リアルタイム計算の表示更新
     */
    updateRealTimeCalculation(formData) {
        const result = this.calculateSimplePremium(
            formData.productId,
            formData.gender,
            formData.entryAge,
            formData.insurancePeriod,
            formData.insuredAmount
        );
        
        this.displayCalculationResult(result);
    }
    
    /**
     * 計算結果の表示
     */
    displayCalculationResult(result) {
        const resultElement = document.getElementById('realtime-result');
        if (!resultElement) return;
        
        if (result.success) {
            resultElement.innerHTML = `
                <div class="realtime-result">
                    <h4>概算保険料</h4>
                    <p>月々: <strong>${this.formatCurrency(result.monthlyPremium)}</strong></p>
                    <p>年間: <strong>${this.formatCurrency(result.annualPremium)}</strong></p>
                    <small>総料率: ${this.formatRate(result.totalRate)}</small>
                </div>
            `;
            resultElement.className = 'realtime-result success';
        } else {
            resultElement.innerHTML = `
                <div class="realtime-result">
                    <p class="error">${result.error}</p>
                </div>
            `;
            resultElement.className = 'realtime-result error';
        }
    }
    
    /**
     * フォーム入力の監視とリアルタイム計算
     */
    initRealTimeCalculation() {
        const form = document.getElementById('premiumForm');
        if (!form) return;
        
        const inputs = form.querySelectorAll('input, select');
        
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                const formData = this.getFormData(form);
                if (this.isFormComplete(formData)) {
                    this.updateRealTimeCalculation(formData);
                }
            });
        });
    }
    
    /**
     * フォームデータの取得
     */
    getFormData(form) {
        return {
            productId: form.querySelector('[name="productId"]')?.value,
            gender: form.querySelector('[name="gender"]')?.value,
            entryAge: parseInt(form.querySelector('[name="entryAge"]')?.value || 0),
            insurancePeriod: parseInt(form.querySelector('[name="insurancePeriod"]')?.value || 0),
            insuredAmount: parseFloat(form.querySelector('[name="insuredAmount"]')?.value || 0)
        };
    }
    
    /**
     * フォーム入力完了チェック
     */
    isFormComplete(formData) {
        return formData.productId && 
               formData.gender && 
               formData.entryAge > 0 && 
               formData.insurancePeriod > 0 && 
               formData.insuredAmount > 0;
    }
    
    /**
     * バッチ計算の準備
     */
    prepareBatchCalculation() {
        const ageRange = document.getElementById('ageRange');
        const periodRange = document.getElementById('periodRange');
        
        if (ageRange && periodRange) {
            ageRange.addEventListener('input', this.validateRangeInput.bind(this));
            periodRange.addEventListener('input', this.validateRangeInput.bind(this));
        }
    }
    
    /**
     * 範囲入力のバリデーション
     */
    validateRangeInput(event) {
        const input = event.target;
        const value = input.value;
        
        if (!/^\d*-\d*$/.test(value)) {
            input.setCustomValidity('「数字-数字」の形式で入力してください');
        } else {
            input.setCustomValidity('');
        }
    }
}

// グローバルインスタンスの作成
window.premiumCalculator = new PremiumCalculator();

// DOM読み込み完了後の初期化
document.addEventListener('DOMContentLoaded', function() {
    // リアルタイム計算の初期化
    premiumCalculator.initRealTimeCalculation();
    
    // バッチ計算の準備
    premiumCalculator.prepareBatchCalculation();
    
    // 保険金額のフォーマット
    const insuredAmountInput = document.getElementById('insuredAmount');
    if (insuredAmountInput) {
        insuredAmountInput.addEventListener('blur', function() {
            const value = parseFloat(this.value);
            if (!isNaN(value)) {
                this.value = value.toLocaleString('ja-JP');
            }
        });
        
        insuredAmountInput.addEventListener('focus', function() {
            this.value = this.value.replace(/[^0-9]/g, '');
        });
    }
});