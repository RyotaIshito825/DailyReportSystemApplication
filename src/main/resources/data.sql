INSERT INTO daily_report_system.employees(name,email,role,password,delete_flg,created_at,updated_at)
     VALUES ("煌木　太郎","exsample@taro.com","ADMIN","$2a$10$vY93/U2cXCfEMBESYnDJUevcjJ208sXav23S.K8elE/J6Sxr4w5jO",0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO daily_report_system.employees(name,email,role,password,delete_flg,created_at,updated_at)
     VALUES ("田中　太郎","exsample@jiro.com","GENERAL","$2a$10$HPIjRCymeRZKEIq.71TDduiEotOlb8Ai6KQUHCs4lGNYlLhcKv4Wi",0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);

INSERT INTO daily_report_system.reports(report_date,title,content,employee_code,delete_flg,created_at,updated_at)
     VALUES (CURRENT_TIMESTAMP,"煌木　太郎の記載、タイトル","煌木　太郎の記載、内容",1,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);
INSERT INTO daily_report_system.reports(report_date,title,content,employee_code,delete_flg,created_at,updated_at)
     VALUES (CURRENT_TIMESTAMP,"田中　太郎の記載、タイトル","田中　太郎の記載、内容",2,0,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP);