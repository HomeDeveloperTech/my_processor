**🧾 Descrição**

**Stakeholder:** Settlement e Produtos Financeiros.

🎯 **Objetivo:**

Como IPB,  
Quero calcular automaticamente a variação mensal do valor de aluguel postado de todas as alianças,

Para identificar desvios significativos de padrão e otimizar a análise da área de Settlement antes que a base de aluguéis seja processada pelo BW.

**Aluguel postado** = valor de aluguel informado pelo IPB para ser processado e faturado no BW no último dia corrido de cada mês.

📎 **Confluence:** IPP Aluguel Postado

**✅ Critério de Aceite:**

**1\. Lógica de cálculo:**

- O cálculo de IPP de aluguel postado deve ser realizado mensalmente, com base na fórmula:  
  **Δ Aluguel Postado = Valor a postar do mês atual (M) - Valor postado do mês anterior (M-1).**
- O cálculo deve ser parametrizável por _service contract_.
- A variação apurada deve ser comparada com o _baseline_ de referência definido por _service contract_, conforme tabela acordada.

| **Aliança** | **Service Contract** | **Faixa de Referência - Aluguel Postado (Δ valor absoluto)** |
| --- | --- | --- |
| Sicredi | 110 | Entre 0 e 500.000 |
| Caixa | 149 | Entre 0 e 500.000 |
| Bin | 125 | Entre 0 e 300.000 |
| Sicoob/Sipag | 104 | Entre -50.000 e 0 |

**2\. Regras de exclusão e observações de negócio:**

- A aliança **Sicoob/Sipag** possui tendência de queda no aluguel devido à saída gradual do processamento de adquirência junto à Fiserv. Portanto, **não se espera aumento** nos valores de aluguel dessa aliança.
- A aliança **Afinz** deve ser **excluída do cálculo**, pois os valores de aluguel incluem outras cobranças não relacionadas à adquirência, o que distorce a análise.

2\. Regras de exclusão e observações de negócio:

A aliança Sicoob/Sipag possui tendência de queda no aluguel devido à saída gradual do processamento de adquirência junto à Fiserv. Portanto, não se espera aumento nos valores de aluguel dessa aliança.

A aliança Afinz deve ser excluída do cálculo, pois os valores de aluguel incluem outras cobranças não relacionadas à adquirência, o que distorce a análise.

3\. Processamento por service contract:

Ao concluir o cálculo para um service contract, o sistema deve:

Gerar um log com o resultado da variação, indicando se está:

▪ Dentro da faixa de referência

▪ Fora da faixa (acima ou abaixo)

Não reprocessar o cálculo, mesmo que esteja fora da faixa.

Prosseguir automaticamente para o próximo service contract pendente, ou finalizar o processo se todos estiverem concluídos.

Cenário

SICREDI:

Aluguel postado no mês anterior = R\$ 20.000.000

Aluguel apurado no mês atual = R\$ 20.680.000

Portanto, houve uma variação de +680.000 neste mês.

🟠 Resultado: Fora da faixa de referência

CAIXA:

Aluguel postado no mês anterior = R\$ 7.500.000

Aluguel apurado no mês atual = R\$ 7.150.000

Portanto, houve uma variação de -350.000 neste mês.

🟠 Resultado: Fora da faixa de referência

BIN:

Aluguel postado no mês anterior = R\$ 2.400.000

Aluguel apurado no mês atual = R\$ 2.700.000

Portanto, houve uma variação de +300.000 neste mês.

🟢 Resultado: Dentro da faixa de referência

SICOOB:

Aluguel postado no mês anterior = R\$ 190.000

Aluguel apurado no mês atual = R\$ 100.000

Portanto, houve uma variação de -90.000 neste mês.

🟠 Resultado: Fora da faixa de referência

SICOOB:

Aluguel postado no mês anterior = R\$ 410.000

Aluguel apurado no mês atual = R\$ 420.000

Portanto, houve uma variação de +10.000 neste mês.

🟠 Resultado: Fora da faixa de referência

**4\. Tratamento de erros técnicos:**

- Em caso de erro técnico no cálculo, o sistema deve:
    - Gravar o erro em log.
    - Reprocessar automaticamente **apenas o service contract afetado**.
    - **Exemplo:** Se o erro ocorrer na Bin, mas Sicredi e Caixa foram processados corretamente, apenas a Bin será reprocessada.

**5\. Finalização do processo:**

- Ao finalizar o cálculo de todos os _service contracts_ com sucesso (sem reprocessamento pendente), o sistema deve registrar um **log de conclusão**.

**6\. Gatilho de relatórios via logs:**

- Cada log gerado durante o processo de cálculo (seja por erro técnico, variação fora da faixa ou conclusão do cálculo) deve **disparar automaticamente a geração de um relatório**.
- A estrutura e formato dos relatórios gerados devem seguir as regras definidas em história específica.