echo "copy and encrypt........."  
echo.  

java -jar classfinal-fatjar.jar -file D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-agent\target\jeepay-agent.jar -packages com.jeequan.jeepay.agent -exclude com.jeequan.jeepay.agent.bootstrap.JeepayAgentApplication -pwd abcd1234 -Y
xcopy /y D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-agent\target\jeepay-agent-encrypted.jar D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\conf\agent\jeepay-agent.jar

java -jar classfinal-fatjar.jar -file D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-manager\target\jeepay-manager.jar -packages com.jeequan.jeepay.mgr -exclude com.jeequan.jeepay.mgr.bootstrap.JeepayMgrApplication -pwd abcd1234 -Y
xcopy /y D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-manager\target\jeepay-manager-encrypted.jar D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\conf\manager\jeepay-manager.jar

java -jar classfinal-fatjar.jar -file D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-merchant\target\jeepay-merchant.jar -packages com.jeequan.jeepay.mch -exclude com.jeequan.jeepay.mch.bootstrap.JeepayMchApplication -pwd abcd1234 -Y
xcopy /y D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-merchant\target\jeepay-merchant-encrypted.jar D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\conf\merchant\jeepay-merchant.jar

xcopy /y D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-payment\target\jeepay-payment.jar D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\conf\payment

java -jar classfinal-fatjar.jar -file D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-robots\target\jeepay-robots.jar -packages com.jeequan.jeepay.com.jeequan -exclude com.jeequan.jeepay.com.jeequan.bootstrap.JeepayRobotsApplication -pwd abcd1234 -Y
xcopy /y D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\jeepay-robots\target\jeepay-robots-encrypted.jar D:\Develop\PayCompany\SourceCode\Aisapay-plus\jeepay-plus\conf\robots\jeepay-robots.jar
