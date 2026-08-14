package com.bigfake.remittance.domain;

import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.RejectReason;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "remittance_file")
public class RemittanceFile extends AuditableEntity {
    private String fileName;
    private String senderBankId;
    private LocalDate fileDate;
    private String controlNumber;
    private String layout;

    @Column(nullable = false)
    private String checksum;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    private RejectReason rejectReason;

    private String rejectMessage;
    private Long originalFileId;
    private Long declaredDetailCount;
    private BigDecimal declaredTotalAmount;
    private Long actualDetailCount;
    private BigDecimal actualTotalAmount;
    private LocalDateTime receivedAt;

    @OneToMany(mappedBy = "remittanceFile", cascade = CascadeType.ALL)
    private List<RemittanceLine> lines = new ArrayList<>();

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getSenderBankId() { return senderBankId; }
    public void setSenderBankId(String senderBankId) { this.senderBankId = senderBankId; }
    public LocalDate getFileDate() { return fileDate; }
    public void setFileDate(LocalDate fileDate) { this.fileDate = fileDate; }
    public String getControlNumber() { return controlNumber; }
    public void setControlNumber(String controlNumber) { this.controlNumber = controlNumber; }
    public String getLayout() { return layout; }
    public void setLayout(String layout) { this.layout = layout; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public FileStatus getStatus() { return status; }
    public void setStatus(FileStatus status) { this.status = status; }
    public RejectReason getRejectReason() { return rejectReason; }
    public void setRejectReason(RejectReason rejectReason) { this.rejectReason = rejectReason; }
    public String getRejectMessage() { return rejectMessage; }
    public void setRejectMessage(String rejectMessage) { this.rejectMessage = rejectMessage; }
    public Long getOriginalFileId() { return originalFileId; }
    public void setOriginalFileId(Long originalFileId) { this.originalFileId = originalFileId; }
    public Long getDeclaredDetailCount() { return declaredDetailCount; }
    public void setDeclaredDetailCount(Long declaredDetailCount) { this.declaredDetailCount = declaredDetailCount; }
    public BigDecimal getDeclaredTotalAmount() { return declaredTotalAmount; }
    public void setDeclaredTotalAmount(BigDecimal declaredTotalAmount) { this.declaredTotalAmount = declaredTotalAmount; }
    public Long getActualDetailCount() { return actualDetailCount; }
    public void setActualDetailCount(Long actualDetailCount) { this.actualDetailCount = actualDetailCount; }
    public BigDecimal getActualTotalAmount() { return actualTotalAmount; }
    public void setActualTotalAmount(BigDecimal actualTotalAmount) { this.actualTotalAmount = actualTotalAmount; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }
    public List<RemittanceLine> getLines() { return lines; }
    public void setLines(List<RemittanceLine> lines) { this.lines = lines; }
}
