# Melhorias na Base de Validações e Enquadramentos do Preço Justo

## Stakeholder
Sicredi e Produtos Financeiros.

## Objetivo
Como IPB, quero implementar **melhorias na base de validações e enquadramentos do Preço Justo**, incluindo ajustes nos campos do relatório e a adição de novos indicadores.

Para otimizar a análise da Fiserv (Produtos Financeiros) antes da confirmação da Sicredi para comunicação e majoração dos clientes no ciclo vigente, além de preparar a estrutura necessária para a implementação de controles automáticos na sequência.

---

## Critérios de Aceite

### 1. Exclusão de campos do relatório de enquadramento

Os seguintes campos devem ser removidos da base de clientes enquadrados no Preço Justo (colunas F, G, H, N, O e Q do arquivo de exemplo):

- FLAG_ENQUADRAMENTO  
- FLAG_COMUNICADO  
- FLAG_MAJORADO  
- FATURAMENTO_MAJORACAO_MIN  
- FATURAMENTO_MAJORACAO_MAX  
- DATA_CREDENCIAMENTO (obtida no campo *data de boarding* do relatório)

---

### 2. Base analítica complementar para Fiserv

A base analítica deve ser gerada em paralelo à base de enquadramentos ("comunicados") validada pela Sicredi e entregue separadamente à Fiserv.

Ela deve conter **5 abas:**

---

## Aba: Visão por EC

**Regras:**  
Todos os ECs (merchants), enquadrados ou não, apresentados ordenadamente por CNPJ.  
Deve, portanto, conter mais registros do que a base enviada à Sicredi ao trazer a quebra por estabelecimento da base completa de clientes processados no Preço Justo, incluindo aqueles que cumpriram o faturamento combinado e, portanto, não foram enquadrados no ciclo vigente.

Campos que informam faturamento M1, M2 e M3 com valores realizados **no ciclo** (pode haver campos zerados).

**Observações Técnicas:**  
- Colunas (A,B,C,D,F,G,H,I,J,K) = `UAT_FICOIPB.PRECO_JUSTO_SICREDI`  
- Colunas (E) = `PRECO_JUSTO_SICREDI_ORIGEM_MERCHANT`  
- Parâmetros: `ANOMES` e `SERVICE_CONTRACT`

---

## Aba: Clientes elegíveis não enquadrados

**Regras:**  
Clientes que atingiram o faturamento combinado e, portanto, não foram enquadrados.  
Taxas atuais do mês no BW, independentemente do ciclo anterior.  
Visão CNPJ.

**Observações Técnicas:**  
Todas as colunas estão na tabela:  
`PRECO_JUSTO_SICREDI_TAXA_ANTECIPACAO`

---

**Obs:**  
O template da base adicional encontra-se anexo à história.  

As bases devem contemplar eventuais casos de **minoração**, ou seja, situações em que a taxa atual do cliente é superior à faixa de majoração definida.

- Nesses casos, o IPB deve calcular e exibir o valor da taxa "majorada", mesmo que inferior à atual, para que o controle de validação consiga detectar corretamente o desvio e garantir a conformidade com a política vigente.

---

## Aba: Aluguel por tecnologia/terminal

**Regras:**  
Apenas clientes no **enquadramento 3**.  
Deve trazer as taxas atuais e a majorar aplicadas por tecnologia e terminal.

**Observações Técnicas:**  
- Colunas (A,B,C,D,J) = `UAT_FICOIPB.PRECO_JUSTO_SICREDI`  
- Colunas (E) = `PRECO_JUSTO_SICREDI_ORIGEM_MERCHANT`  
- Colunas (F,G,H) = `PRECO_JUSTO_SICREDI_ORIGEM_TERMINAL`  
- Colunas (I) = `PRECO_JUSTO_SICREDI_TAXA_ALUGUEL`

---

## Aba: MDR por bandeira e produto

**Regras:**  
Apenas clientes no **enquadramento 3**.  
Deve trazer as taxas atuais e a majorar aplicadas por bandeira e produto.

**Observações Técnicas:**  
Alinhar Template com **Cura, Tayanne (São Paulo)**

---

## Aba: Antecipação (automática e eventual)

**Regras:**  
Apenas clientes no **enquadramento 3**.  
Deve trazer as taxas atuais e a majorar aplicadas por tipo (automática e eventual).

**Observações Técnicas:**  
- Colunas (A,B,C,D) = `UAT_FICOIPB.PRECO_JUSTO_SICREDI`  
- Colunas (E) = `PRECO_JUSTO_SICREDI_ORIGEM_MERCHANT`  
- Colunas (F,H) = `PRECO_JUSTO_SICREDI_ORIGEM_TAXAS_ANTECIPACAO`  
- Colunas (G,I) = `PRECO_JUSTO_SICREDI_TAXA_ANTECIPACAO`

---

## ⚠️ Cenário de Teste
(Conteúdo não visível nas imagens)
