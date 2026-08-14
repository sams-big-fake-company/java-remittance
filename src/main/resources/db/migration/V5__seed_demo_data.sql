INSERT INTO payer(payer_code,name,status,email,address_line1,city,state,postal_code,country,is_enabled)
VALUES ('ACME000001','Acme Manufacturing','ACTIVE','ar@acme.example','100 Main Street','Chicago','IL','60601','US','Y'),
 ('GLOBEX0001','Globex Corporation','ACTIVE','finance@globex.example','200 Market Street','New York','NY','10001','US','Y'),
 ('INITECH001','Initech','ACTIVE','ap@initech.example','300 Office Park','Austin','TX','78701','US','Y'),
 ('UMBRELLA01','Umbrella Trading','ACTIVE','billing@umbrella.example','400 Lake Road','Boston','MA','02108','US','Y'),
 ('OLDPAYER01','Old Payer Incorporated','INACTIVE','legacy@example.com','500 Old Road','Denver','CO','80202','US','N');
INSERT INTO open_invoice(payer_code,invoice_reference,normalized_reference,invoiced_amount,outstanding_amount,due_date,status)
VALUES ('ACME000001','INV-100045','INV100045',1500.00,1500.00,CURRENT_DATE,'OPEN'),
 ('ACME000001','INV-100046','INV100046',2500.00,2500.00,CURRENT_DATE,'OPEN'),
 ('ACME000001','INV-100047','INV100047',750.00,750.00,CURRENT_DATE,'OPEN'),
 ('GLOBEX0001','INV-200010','INV200010',1250.00,1250.00,CURRENT_DATE,'OPEN'),
 ('GLOBEX0001','INV-200011','INV200011',5000.00,5000.00,CURRENT_DATE,'OPEN'),
 ('INITECH001','INV-300001','INV300001',325.00,325.00,CURRENT_DATE,'OPEN'),
 ('INITECH001','INV-300002','INV300002',900.00,900.00,CURRENT_DATE,'OPEN'),
 ('UMBRELLA01','INV-400100','INV400100',2200.00,2200.00,CURRENT_DATE,'OPEN'),
 ('UMBRELLA01','INV-400101','INV400101',1800.00,1800.00,CURRENT_DATE,'OPEN'),
 ('ACME000001','INV-100048','INV100048',99.99,99.99,CURRENT_DATE,'OPEN');
