**Descrição**

**Stakeholder:** Settlement e Produtos Financeiros.

🎯 **Objetivo:**

Como IPB,  
Quero gerar relatórios parciais e finais dos cálculos de IPP referentes ao **aluguel postado**,

Para garantir que o processo está ocorrendo conforme o esperado e automatizar a configuração dos parâmetros analisados pelo time de Settlement.

📎 **Confluence:** IPP Aluguel Postado

✅ **Critério de Aceite:**

**1\. Estrutura e nomenclatura dos relatórios:**

- Os relatórios devem seguir um **template padrão**, conforme modelo proposto em anexo.
- Os arquivos devem ser nomeados conforme o tipo e período de apuração:

| **Títulos** | **Abas** |
| --- | --- |
| **Relatórios parciais**, gerados à medida que os cálculos de aluguel são concluídos por _service contract_ → **PARCIAL_IPP-ALUGUEL_POSTADO_MMAA**  <br>_Ex:_ PARCIAL_IPP-ALUGUEL_POSTADO_0825 | **Consolidado_MMAA**  <br>_Ex:_ Consolidado_0825 |
| **Relatório final**, consolidando os resultados do IPP calculado para o mês corrente, após a conclusão de todos os _service contracts_ → **FINAL_IPP_ALUGUEL_POSTADO_MMAA**  <br>_Ex:_ FINAL_IPP_ALUGUEL_POSTADO_0825 | **Analítico_SERVICE CONTRACT_MMAA**  <br>_Ex:_ Analítico_SICREDI_0825 |

**2\. Visão consolidada:**

- **Apresenta o resultado da variação calculada por aliança/_service contract_.**
- **Deve manter o histórico de atualizações, permitindo rastreabilidade do processo.**
- **Registros organizados em ordem cronológica de evento, do mais antigo ao mais recente.**
- **Deve indicar:**
    - **Incidência de erros de cálculo.**
    - **Resultados de variação por _service contract_.**
    - **Quantidade de registros por contrato (inclusive múltiplas linhas, se houver erro + resultado válido após reprocessamento).**
    - **_Service contracts_ ainda pendentes de cálculo.**

**3\. Visão analítica:**

- **Detalha todos os terminais de todos os _merchants_ credenciados.**
- **Cada aba representa um service contract específico.**
- **Deve incluir a data de desinstalação do terminal (consumida do BW), para rastrear aluguéis cobrados parcialmente (_pró-rata_).**
    - **Se o campo estiver vazio, considera-se que o terminal está ativo (_Device Status = 1_).**

**4\. Gatilho de geração dos relatórios:**

- A geração dos relatórios deve ser **automática**, com base nos logs gerados durante o cálculo:
    - **Log de variação** → Gera relatório parcial com os resultados apurados até o momento.
    - **Log de erro técnico/cálculo** → Gera relatório parcial com os resultados apurados e indicação do erro no _service contract_ correspondente.
    - **Log de conclusão** → Gera relatório final consolidado, após todos os _service contracts_ serem processados com sucesso.
- A cada _service contract_ **finalizado**, o sistema deve:
    - Gerar um **relatório parcial** com os dados apurados até o momento.
    - Atualizar a aba **Consolidado** com os contratos pendentes.

**5\. Condições para geração do relatório final:**

- O relatório final só deve ser gerado quando:
    - Todos os _service contracts_ tiverem **resultado válido de variação** (dentro ou fora da faixa).
    - **Não houver reprocessamento pendente.**

6\. Formato dos arquivos:

Os relatórios devem ser gerados e disponibilizados nos formatos .xlsx / .csv, para facilitar a análise pelas áreas envolvidas.

Se basear na geração dos relatórios existentes, pois são iguais os cabeçalhos com exceção de incluir o valor postado uma coluna antes da coluna "valor origem".