package TeachWork.example.TeachWork.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class PdfAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(PdfAnalysisService.class);

    // Belge türleri ve ilgili anahtar kelimeler
    private static final Map<String, List<String>> DOCUMENT_TYPES = new HashMap<>();
    static {
        DOCUMENT_TYPES.put("CV", Arrays.asList(
            "cv", "özgeçmiş", "curriculum vitae", "kişisel bilgiler", "eğitim", "deneyim",
            "iş deneyimi", "beceriler", "yetenekler", "referanslar", "iletişim bilgileri",
            "kişisel bilgiler", "doğum tarihi", "adres", "telefon", "e-posta"
        ));
        DOCUMENT_TYPES.put("Ödev", Arrays.asList(
            "ödev", "assignment", "proje", "görev", "soru", "cevap", "çözüm",
            "kaynakça", "referanslar", "kaynak", "kaynaklar", "teslim tarihi",
            "öğrenci no", "öğrenci numarası", "ders kodu", "ders adı", "öğretim üyesi"
        ));
        DOCUMENT_TYPES.put("Rapor", Arrays.asList(
            "rapor", "report", "analiz", "sonuç", "bulgular", "tartışma",
            "öneriler", "yöntem", "metodoloji", "veri", "istatistik", "grafik",
            "tablo", "değerlendirme", "araştırma", "anket", "görüşme"
        ));
        DOCUMENT_TYPES.put("Makale", Arrays.asList(
            "makale", "article", "araştırma", "research", "literatür", "literature",
            "abstract", "özet", "giriş", "introduction", "metod", "method",
            "kaynakça", "referanslar", "doi", "keywords", "anahtar kelimeler"
        ));
    }

    public List<String> analyzePdf(String filePath) {
        List<String> suggestions = new ArrayList<>();
        try {
            // PDF'den metin çıkar
            String text = extractTextFromPdf(filePath);
            logger.info("PDF'den metin çıkarıldı, uzunluk: {}", text.length());
            
            // Belge türünü tespit et
            String documentType = detectDocumentType(text);
            suggestions.add("Belge Türü: " + documentType);
            
            // Belge içeriğini analiz et
            String contentSummary = analyzeContent(text, documentType);
            suggestions.add("İçerik Özeti: " + contentSummary);
            
            // Önemli noktaları bul
            List<String> importantPoints = findImportantPoints(text);
            if (!importantPoints.isEmpty()) {
                suggestions.add("\nÖnemli Noktalar:");
                suggestions.addAll(importantPoints);
            }
            
            logger.info("Analiz tamamlandı, {} öneri bulundu", suggestions.size());
            
        } catch (Exception e) {
            logger.error("PDF analizi sırasında hata oluştu", e);
            suggestions.add("PDF analizi sırasında bir hata oluştu: " + e.getMessage());
        }
        return suggestions;
    }

    private String extractTextFromPdf(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String detectDocumentType(String text) {
        text = text.toLowerCase();
        Map<String, Double> typeScores = new HashMap<>();
        
        // Her belge türü için puan hesapla
        for (Map.Entry<String, List<String>> entry : DOCUMENT_TYPES.entrySet()) {
            double score = 0;
            int keywordCount = 0;
            
            // Anahtar kelimeleri kontrol et
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword.toLowerCase())) {
                    score += 1.0;
                    keywordCount++;
                }
            }
            
            // Belge türüne özgü yapısal özellikleri kontrol et
            switch (entry.getKey()) {
                case "CV":
                    if (containsCvStructure(text)) {
                        score += 3.0; // CV yapısı için daha yüksek puan
                    } else {
                        score -= 2.0; // CV yapısı yoksa puan düşür
                    }
                    break;
                case "Ödev":
                    if (containsAssignmentStructure(text)) {
                        score += 3.0;
                    }
                    break;
                case "Rapor":
                    if (containsReportStructure(text)) {
                        score += 3.0;
                    }
                    break;
                case "Makale":
                    if (containsArticleStructure(text)) {
                        score += 3.0;
                    }
                    break;
            }
            
            // Anahtar kelimelerin metin içindeki yoğunluğunu hesapla
            if (keywordCount > 0) {
                double density = (double) keywordCount / entry.getValue().size();
                score *= (1 + density);
            }
            
            // Belge türüne göre ek kontroller
            switch (entry.getKey()) {
                case "CV":
                    // CV için ek kontroller
                    if (!containsPersonalInfo(text)) {
                        score -= 2.0; // Kişisel bilgi yoksa puan düşür
                    }
                    if (!containsEducationOrExperience(text)) {
                        score -= 2.0; // Eğitim veya deneyim yoksa puan düşür
                    }
                    break;
                case "Ödev":
                    // Ödev için ek kontroller
                    if (containsStudentInfo(text)) {
                        score += 2.0;
                    }
                    if (containsDeadline(text)) {
                        score += 2.0;
                    }
                    break;
                case "Rapor":
                    // Rapor için ek kontroller
                    if (containsDataAnalysis(text)) {
                        score += 2.0;
                    }
                    if (containsConclusions(text)) {
                        score += 2.0;
                    }
                    break;
                case "Makale":
                    // Makale için ek kontroller
                    if (containsAbstract(text)) {
                        score += 2.0;
                    }
                    if (containsReferences(text)) {
                        score += 2.0;
                    }
                    break;
            }
            
            typeScores.put(entry.getKey(), score);
        }
        
        // En yüksek puanlı belge türünü bul
        Map.Entry<String, Double> maxEntry = typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        // Eğer en yüksek puan çok düşükse "Bilinmeyen Belge" olarak işaretle
        if (maxEntry == null || maxEntry.getValue() < 3.0) {
            return "Bilinmeyen Belge";
        }
        
        return maxEntry.getKey();
    }

    private boolean containsPersonalInfo(String text) {
        String[] personalInfo = {
            "ad", "soyad", "doğum tarihi", "doğum yeri", "adres", "telefon",
            "e-posta", "email", "iletişim", "kişisel bilgiler"
        };
        return Arrays.stream(personalInfo).anyMatch(text::contains);
    }

    private boolean containsEducationOrExperience(String text) {
        return text.contains("eğitim") || text.contains("education") ||
               text.contains("deneyim") || text.contains("experience") ||
               text.contains("iş") || text.contains("work");
    }

    private boolean containsStudentInfo(String text) {
        return text.contains("öğrenci") || text.contains("student") ||
               text.contains("öğrenci no") || text.contains("student id") ||
               text.contains("öğrenci numarası");
    }

    private boolean containsDeadline(String text) {
        return text.contains("teslim") || text.contains("deadline") ||
               text.contains("son tarih") || text.contains("due date");
    }

    private boolean containsDataAnalysis(String text) {
        return text.contains("veri") || text.contains("data") ||
               text.contains("analiz") || text.contains("analysis") ||
               text.contains("istatistik") || text.contains("statistics");
    }

    private boolean containsConclusions(String text) {
        return text.contains("sonuç") || text.contains("conclusion") ||
               text.contains("tartışma") || text.contains("discussion") ||
               text.contains("öneriler") || text.contains("recommendations");
    }

    private boolean containsAbstract(String text) {
        return text.contains("abstract") || text.contains("özet") ||
               text.contains("summary");
    }

    private boolean containsReferences(String text) {
        return text.contains("kaynakça") || text.contains("references") ||
               text.contains("referanslar") || text.contains("bibliography");
    }

    private boolean containsCvStructure(String text) {
        // CV'ye özgü yapısal özellikler
        String[] sections = {
            "kişisel bilgiler", "eğitim", "deneyim", "beceriler", "referanslar",
            "personal information", "education", "experience", "skills", "references"
        };
        int sectionCount = 0;
        for (String section : sections) {
            if (text.contains(section)) sectionCount++;
        }
        return sectionCount >= 4; // En az 4 bölüm varsa CV olabilir
    }

    private boolean containsAssignmentStructure(String text) {
        // Ödeve özgü yapısal özellikler
        boolean hasStudentInfo = containsStudentInfo(text);
        boolean hasDeadline = containsDeadline(text);
        boolean hasTask = text.contains("soru") || text.contains("görev") ||
                         text.contains("assignment") || text.contains("task");
        boolean hasCourseInfo = text.contains("ders") || text.contains("course") ||
                              text.contains("öğretim üyesi") || text.contains("instructor");
        
        return (hasStudentInfo && hasDeadline) || (hasTask && hasCourseInfo);
    }

    private boolean containsReportStructure(String text) {
        // Rapor yapısına özgü özellikler
        String[] sections = {
            "giriş", "yöntem", "sonuç", "tartışma", "öneriler",
            "introduction", "method", "results", "discussion", "recommendations"
        };
        int sectionCount = 0;
        for (String section : sections) {
            if (text.contains(section)) sectionCount++;
        }
        return sectionCount >= 3; // En az 3 bölüm varsa rapor olabilir
    }

    private boolean containsArticleStructure(String text) {
        // Makale yapısına özgü özellikler
        boolean hasAbstract = containsAbstract(text);
        boolean hasIntroduction = text.contains("introduction") || text.contains("giriş");
        boolean hasConclusion = text.contains("conclusion") || text.contains("sonuç");
        boolean hasReferences = containsReferences(text);
        
        return (hasAbstract && hasIntroduction) || (hasConclusion && hasReferences);
    }

    private String analyzeContent(String text, String documentType) {
        text = text.toLowerCase();
        StringBuilder summary = new StringBuilder();
        
        switch (documentType) {
            case "CV":
                // İsim ve kişisel bilgileri bul
                String name = findName(text);
                if (name != null) {
                    summary.append(name).append(" adlı kişinin ");
                }
                summary.append("özgeçmiş belgesi. ");
                
                // Eğitim ve deneyim bilgilerini kontrol et
                if (text.contains("eğitim") || text.contains("education")) {
                    summary.append("Eğitim bilgileri içeriyor. ");
                }
                if (text.contains("deneyim") || text.contains("experience")) {
                    summary.append("İş deneyimi bilgileri mevcut. ");
                }
                break;
                
            case "Ödev":
                summary.append("Ödev/proje çalışması. ");
                if (text.contains("kod") || text.contains("programlama") || text.contains("yazılım")) {
                    summary.append("Kodlama/programlama ile ilgili bir çalışma. ");
                }
                if (text.contains("soru") || text.contains("cevap")) {
                    summary.append("Soru-cevap formatında bir ödev. ");
                }
                // Ödev konusunu bul
                String topic = findAssignmentTopic(text);
                if (topic != null) {
                    summary.append("Konu: ").append(topic).append(". ");
                }
                break;
                
            case "Rapor":
                summary.append("Analiz/rapor belgesi. ");
                if (text.contains("veri") || text.contains("istatistik")) {
                    summary.append("Veri analizi ve istatistikler içeriyor. ");
                }
                // Rapor konusunu bul
                String reportTopic = findReportTopic(text);
                if (reportTopic != null) {
                    summary.append("Konu: ").append(reportTopic).append(". ");
                }
                break;
                
            case "Makale":
                summary.append("Araştırma makalesi. ");
                if (text.contains("abstract") || text.contains("özet")) {
                    summary.append("Özet bölümü mevcut. ");
                }
                // Makale konusunu bul
                String articleTopic = findArticleTopic(text);
                if (articleTopic != null) {
                    summary.append("Konu: ").append(articleTopic).append(". ");
                }
                break;
                
            default:
                summary.append("Belge içeriği analiz edildi. ");
        }
        
        return summary.toString();
    }

    private String findName(String text) {
        // İlk satırı al (genellikle isim burada olur)
        String firstLine = text.split("\n")[0].trim();
        // Sadece harflerden oluşan kelimeleri bul
        String[] words = firstLine.split("\\s+");
        if (words.length >= 2) {
            return words[0] + " " + words[1];
        }
        return null;
    }

    private String findAssignmentTopic(String text) {
        // Ödev konusunu bulmaya çalış
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.toLowerCase().trim();
            if (line.contains("konu:") || line.contains("topic:") || 
                line.contains("ödev:") || line.contains("assignment:")) {
                return line.split(":")[1].trim();
            }
        }
        return null;
    }

    private String findReportTopic(String text) {
        // Rapor konusunu bulmaya çalış
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.toLowerCase().trim();
            if (line.contains("rapor:") || line.contains("report:") || 
                line.contains("konu:") || line.contains("topic:")) {
                return line.split(":")[1].trim();
            }
        }
        return null;
    }

    private String findArticleTopic(String text) {
        // Makale konusunu bulmaya çalış
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.toLowerCase().trim();
            if (line.contains("title:") || line.contains("başlık:") || 
                line.contains("konu:") || line.contains("topic:")) {
                return line.split(":")[1].trim();
            }
        }
        return null;
    }

    private List<String> findImportantPoints(String text) {
        List<String> points = new ArrayList<>();
        String[] sentences = text.split("[.!?]");
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.length() > 0) {
                // Önemli anahtar kelimeleri kontrol et
                if (containsImportantKeywords(sentence)) {
                    points.add("• " + sentence);
                }
                
                // Sayısal değerleri kontrol et
                if (containsNumericValues(sentence)) {
                    points.add("• Sayısal değer: " + sentence);
                }
                
                // Tarihleri kontrol et
                if (containsDates(sentence)) {
                    points.add("• Tarih: " + sentence);
                }
            }
        }
        
        return points;
    }

    private boolean containsImportantKeywords(String text) {
        String[] keywords = {
            "önemli", "dikkat", "not", "öneri", "tavsiye", "dikkat edilmeli",
            "önemli nokta", "kritik", "acil", "öncelikli", "dikkat edilmesi gereken",
            "özetle", "sonuç olarak", "özellikle", "mutlaka", "kesinlikle",
            "lütfen", "dikkatli olun", "unutmayın", "hatırlatma", "uyarı"
        };
        
        text = text.toLowerCase();
        return Arrays.stream(keywords).anyMatch(text::contains);
    }

    private boolean containsNumericValues(String text) {
        return text.matches(".*\\d+([.,]\\d+)?%?.*");
    }

    private boolean containsDates(String text) {
        return text.matches(".*\\d{1,2}[./]\\d{1,2}[./]\\d{2,4}.*");
    }
}