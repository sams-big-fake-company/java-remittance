INSERT INTO payer(payer_code,name,status,email,address_line1,city,state,postal_code,country,is_enabled,
                  created_at,created_by,updated_at,updated_by,version)
WITH seed_constants AS (
    SELECT 'ACTIVE' AS active_status, 'INACTIVE' AS inactive_status,
           'Y' AS enabled_flag, 'N' AS disabled_flag, 'seed' AS actor,
           CURRENT_TIMESTAMP AS audit_time, 0 AS initial_version,
           'ACME000001' AS acme_payer, 'GLOBEX0001' AS globex_payer,
           'INITECH001' AS initech_payer, 'UMBRELLA01' AS umbrella_payer,
           'OLDPAYER01' AS old_payer
),
seed_payers(payer_code,name,email,address_line1,city,state,postal_code,country,active,enabled) AS (
    VALUES ((SELECT acme_payer FROM seed_constants),'Acme Manufacturing','ar@acme.example',
            '100 Main Street','Chicago','IL','60601','US',1,1),
           ((SELECT globex_payer FROM seed_constants),'Globex Corporation','finance@globex.example',
            '200 Market Street','New York','NY','10001','US',1,1),
           ((SELECT initech_payer FROM seed_constants),'Initech','ap@initech.example',
            '300 Office Park','Austin','TX','78701','US',1,1),
           ((SELECT umbrella_payer FROM seed_constants),'Umbrella Trading','billing@umbrella.example',
            '400 Lake Road','Boston','MA','02108','US',1,1),
           ((SELECT old_payer FROM seed_constants),'Old Payer Incorporated','legacy@example.com',
            '500 Old Road','Denver','CO','80202','US',0,0)
)
SELECT p.payer_code, p.name,
       CASE WHEN p.active = 1 THEN (SELECT active_status FROM seed_constants)
            ELSE (SELECT inactive_status FROM seed_constants) END,
       p.email, p.address_line1, p.city, p.state, p.postal_code, p.country,
       CASE WHEN p.enabled = 1 THEN (SELECT enabled_flag FROM seed_constants)
            ELSE (SELECT disabled_flag FROM seed_constants) END,
       (SELECT audit_time FROM seed_constants), (SELECT actor FROM seed_constants),
       (SELECT audit_time FROM seed_constants), (SELECT actor FROM seed_constants),
       (SELECT initial_version FROM seed_constants)
FROM seed_payers p;

INSERT INTO open_invoice(payer_code,invoice_reference,normalized_reference,invoiced_amount,outstanding_amount,
                         due_date,status,created_at,created_by,updated_at,updated_by,version)
WITH seed_constants AS (
    SELECT 'OPEN' AS open_status, 'seed' AS actor,
           CURRENT_DATE AS due_date, CURRENT_TIMESTAMP AS audit_time, 0 AS initial_version,
           'ACME000001' AS acme_payer, 'GLOBEX0001' AS globex_payer,
           'INITECH001' AS initech_payer, 'UMBRELLA01' AS umbrella_payer
),
seed_invoices(payer_code,invoice_reference,normalized_reference,invoiced_amount) AS (
    VALUES ((SELECT acme_payer FROM seed_constants),'INV-100045','INV100045',1500.00),
           ((SELECT acme_payer FROM seed_constants),'INV-100046','INV100046',2500.00),
           ((SELECT acme_payer FROM seed_constants),'INV-100047','INV100047',750.00),
           ((SELECT globex_payer FROM seed_constants),'INV-200010','INV200010',1250.00),
           ((SELECT globex_payer FROM seed_constants),'INV-200011','INV200011',5000.00),
           ((SELECT initech_payer FROM seed_constants),'INV-300001','INV300001',325.00),
           ((SELECT initech_payer FROM seed_constants),'INV-300002','INV300002',900.00),
           ((SELECT umbrella_payer FROM seed_constants),'INV-400100','INV400100',2200.00),
           ((SELECT umbrella_payer FROM seed_constants),'INV-400101','INV400101',1800.00),
           ((SELECT acme_payer FROM seed_constants),'INV-100048','INV100048',99.99)
)
SELECT i.payer_code, i.invoice_reference, i.normalized_reference, i.invoiced_amount, i.invoiced_amount,
       (SELECT due_date FROM seed_constants), (SELECT open_status FROM seed_constants),
       (SELECT audit_time FROM seed_constants), (SELECT actor FROM seed_constants),
       (SELECT audit_time FROM seed_constants), (SELECT actor FROM seed_constants),
       (SELECT initial_version FROM seed_constants)
FROM seed_invoices i;
