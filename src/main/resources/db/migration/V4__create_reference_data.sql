CREATE TABLE deduction_reason_code (
 code VARCHAR(20) PRIMARY KEY, description VARCHAR(200) NOT NULL, active CHAR(1) NOT NULL DEFAULT 'Y'
);
INSERT INTO deduction_reason_code(code,description,active) VALUES
 ('SHRT','Short payment','Y'),('DAMG','Damaged goods','Y'),('DISC','Early payment discount','Y'),
 ('FRGT','Freight disallowed','Y'),('PRIC','Price discrepancy','Y'),('RTRN','Returned goods','Y'),
 ('UNDR','Unexplained underpayment','Y');
