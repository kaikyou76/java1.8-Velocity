package com.insurance.batch;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * バッチ処理起動クラス
 * アプリケーション起動時にすべてのバッチ処理を開始
 */
@WebListener
public class BatchStarter implements ServletContextListener {
    
    /**
     * アプリケーション起動時にバッチ処理を開始
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("保険システムバッチ処理を開始します...");
        
        try {
            // 保険料更新バッチを開始
            PremiumUpdateBatch.startBatchProcessing();
            System.out.println("保険料更新バッチを開始しました");
            
            // 契約ステータス更新バッチを開始
            ContractStatusBatch.startBatchProcessing();
            System.out.println("契約ステータス更新バッチを開始しました");
            
            // レポート生成バッチを開始
            ReportGenerationBatch.startBatchProcessing();
            System.out.println("レポート生成バッチを開始しました");
            
            System.out.println("すべてのバッチ処理が正常に開始されました");
            
        } catch (Exception e) {
            System.err.println("バッチ処理起動中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * アプリケーション終了時にバッチ処理を停止
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("保険システムバッチ処理を停止します...");
        
        try {
            // 保険料更新バッチを停止
            PremiumUpdateBatch.stopBatchProcessing();
            System.out.println("保険料更新バッチを停止しました");
            
            // 契約ステータス更新バッチを停止
            ContractStatusBatch.stopBatchProcessing();
            System.out.println("契約ステータス更新バッチを停止しました");
            
            // レポート生成バッチを停止
            ReportGenerationBatch.stopBatchProcessing();
            System.out.println("レポート生成バッチを停止しました");
            
            System.out.println("すべてのバッチ処理が正常に停止されました");
            
        } catch (Exception e) {
            System.err.println("バッチ処理停止中にエラーが発生しました: " + e.getMessage());
            e.printStackTrace();
        }
    }
}