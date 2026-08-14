WITH seed_constants AS (
    SELECT 'ACTIVE' AS active_status, 'INACTIVE' AS inactive_status,
           'Y' AS enabled_flag, 'N' AS disabled_flag, 'seed' AS actor,
           CURRENT_TIMESTAMP AS audit_time, 0 AS initial_version
),
seed_payers(payer_code,name,email,address_line1,city,state,postal_code,country,active,enabled) AS (
    VALUES ('ACME000001','Acme Manufacturing','ar@acme.example','100 Main Street','Chicago','IL','60601','US',TRUE,TRUE),
           ('GLOBEX0001','Globex Corporation','finance@globex.example','200 Market Street','New York','NY','10001','US',TRUE,TRUE),
           ('INITECH001','Initech','ap@initech.example','300 Office Park','Austin','TX','78701','US',TRUE,TRUE),
           ('UMBRELLA01','Umbrella Trading','billing@umbrella.example','400 Lake Road','Boston','MA','02108','US',TRUE,TRUE),
           ('OLDPAYER01','Old Payer Incorporated','legacy@example.com','500 Old Road','Denver','CO','80202','US',FALSE,FALSE)
)
INSERT INTO payer(payer_code,name,status,email,address_line1,city,state,postal_code,country,is_enabled,
                  created_at,created_by,updated_at,updated_by,version)
SELECT p.payer_code, p.name,
       CASE WHEN p.active THEN c.active_status ELSE c.inactive_status END,
       p.email, p.address_line1, p.city, p.state, p.postal_code, p.country,
       CASE WHEN p.enabled THEN c.enabled_flag ELSE c.disabled_flag END,
       c.audit_time, c.actor, c.audit_time, c.actor, c.initial_version
FROM seed_payers p
CROSS JOIN seed_constants c;

WITH seed_constants AS (
    SELECT 'OPEN' AS open_status, 'seed' AS actor,
           CURRENT_DATE AS due_date, CURRENT_TIMESTAMP AS audit_time, 0 AS initial_version
),
seed_invoices(payer_code,invoice_reference,normalized_reference,invoiced_amount) AS (
    VALUES ('ACME000001','INV-100045','INV100045',1500.00),
           ('ACME000001','INV-100046','INV100046',2500.00),
           ('ACME000001','INV-100047','INV100047',750.00),
           ('GLOBEX0001','INV-200010','INV200010',1250.00),
           ('GLOBEX0001','INV-200011','INV200011',5000.00),
           ('INITECH001','INV-300001','INV300001',325.00),
           ('INITECH001','INV-300002','INV300002',900.00),
           ('UMBRELLA01','INV-400100','INV400100',2200.00),
           ('UMBRELLA01','INV-400101','INV400101',1800.00),
           ('ACME000001','INV-100048','INV100048',99.99)
)
INSERT INTO open_invoice(payer_code,invoice_reference,normalized_reference,invoiced_amount,outstanding_amount,
                         due_date,status,created_at,created_by,updated_at,updated_by,version)
SELECT i.payer_code, i.invoice_reference, i.normalized_reference, i.invoiced_amount, i.invoiced_amount,
       c.due_date, c.open_status, c.audit_time, c.actor, c.audit_time, c.actor, c.initial_version
FROM seed_invoices i
CROSS JOIN seed_constants c;
