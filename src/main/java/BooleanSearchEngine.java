import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    private Map<String, List<PageEntry>> pageWords = new HashMap<>();
    private final Set<String> stopWords = new HashSet<>();
    private final File stopList = new File("stop-ru.txt");


    private void StopList() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(stopList))) {
            String line = br.readLine();
            while (line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
        }
    }

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        StopList();
        File[] pdfsFiles = pdfsDir.listFiles();
        for (var pdf : pdfsFiles) {
            var doc = new PdfDocument(new PdfReader(pdf));
            var pages = doc.getNumberOfPages();
            for (int i = 1; i < pages; i++) {
                var text = PdfTextExtractor.getTextFromPage(doc.getPage(i));
                var words = text.split("\\P{IsAlphabetic}+");

                Map<String, Integer> freqs = new HashMap<>();
                for (var word : words) {
                    if ((word.isEmpty()) | (stopWords.contains(word.toLowerCase()))) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }
                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    var word = entry.getKey();
                    var count = entry.getValue();
                    PageEntry pageEntry = new PageEntry(pdf.getName(), i, count);

                    if (pageWords.containsKey(word)) {
                        pageWords.get(word).add(pageEntry);
                    } else {
                        pageWords.computeIfAbsent(word, w -> new ArrayList<>()).add(pageEntry);
                    }
                }
            }
        }
    }


    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> tempList = new ArrayList<>();
        List<PageEntry> resultList = new ArrayList<>();
        String[] request = word.toLowerCase().split("\\P{IsAlphabetic}+");
        for (String newRequest : request) {
            if (pageWords.get(newRequest) != null) {
                tempList.addAll(pageWords.get(newRequest));
            }
        }
        Map<String, Map<Integer, Integer>> pageAndCount = new HashMap<>();
        for (PageEntry pageEntry : tempList) {
            pageAndCount.computeIfAbsent(pageEntry.getPdfName(), key -> new HashMap<>()).merge(pageEntry.getPage(), pageEntry.getCount(), Integer::sum);
        }

        pageAndCount.forEach((key, value) -> {
            for (var temp : value.entrySet()) {
                resultList.add(new PageEntry(key, temp.getKey(), temp.getValue()));
            }
        });
        Collections.sort(resultList);
        return resultList;
    }
}