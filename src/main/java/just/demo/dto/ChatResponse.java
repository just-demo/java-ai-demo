package just.demo.dto;

import java.util.List;

public record ChatResponse(String answer, List<String> documentsUsed) {
}
