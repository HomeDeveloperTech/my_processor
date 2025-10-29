package com.fiserv.fico.service;

import com.fiserv.fico.domain.PrecoJustoSicredi;
import com.fiserv.fico.repository.PrecoJustoSicrediRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrecoJustoService {

  private final PrecoJustoSicrediRepository precoJustoRepo;
  private final PrecoJustoXlsxWriter xlsxWriter = new PrecoJustoXlsxWriter();

  public void generate(String anomes, String serviceContract, Path outDir) {
    try {
      if (outDir == null) {
        outDir = Path.of("./out");
      }
      Files.createDirectories(outDir);

      // Buscar dados na tabela PRECO_JUSTO_SICREDI
      List<PrecoJustoSicredi> registros = precoJustoRepo.findByAnomesAndServiceContract(anomes, serviceContract);

      // Gerar workbook Excel conforme guideline
      Path outFile = outDir.resolve(fileName(anomes, serviceContract));
      xlsxWriter.writeWorkbook(anomes, serviceContract, registros, outFile);

      // Registrar marcador de execução
      Files.writeString(outDir.resolve("PRECO_JUSTO_EXECUTION.log"),
          "Executado em " + LocalDateTime.now() + " anomes=" + anomes + " service=" + serviceContract + " arquivo=" + outFile + System.lineSeparator(),
          StandardCharsets.UTF_8,
          Files.exists(outDir.resolve("PRECO_JUSTO_EXECUTION.log")) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
    } catch (IOException e) {
      throw new IllegalStateException("Falha ao gerar arquivo Excel do processo Preço Justo", e);
    }
  }

  private String fileName(String anomes, String service) {
    String a = anomes == null ? "" : anomes;
    String s = service == null ? "" : service;
    return String.format("preco_justo_%s_%s.xlsx", a, s);
  }
}
