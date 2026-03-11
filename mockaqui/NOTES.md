# Technical Debt

- Encontrar maneira melhor de expor os uses cases, ou seja, alguma forma
  que possibilite export as interfaces para as controllers

# Modo de Gravação

- Pensei em duas estratégias de implementação diferentes para essa funcionalidade, onde:
    - a primeira se trata de um switch manual via properties na API que está utilizando a lib
    - a segunda se trata de um switch automático via interface web.

Detalhes da Primeira Implementação:
Para o primeiro caso, o usuário deverá modificar nas properties do spring explicitando que deseja interceptar as
requests para gravar as respostas, headers e status code do serviço original. A lib vai consultar as properties em toda
requisição validando se deve enviar os dadaos interceptados do serviço original para a API de mock para gravar;

- Pontos positivos
    - Menor número de network calls por requisição do mock, ou seja, na prática a lib vai fazer 2 chamadas quando
      estiver em modo de gravação, a primeira para o serviço original e a segunda enviando os dados recebidos do serviço
      para a API de mock registrando as informações.
- Pontos negativos
    - Dependência de ação manual para cada vez que desejar gravar uma requisição, logo, se o serviço estiver em outro
      ambiente vai precisar alterar as properties, reiniciar o POD, interceptar a resposta, alterar o properties
      novamente desativando o switch de gravação e reiniciar o POD novamente.

Detalhes da Segunda Implementação:
Na segunda implementação não haverá ajuste manual via properties na API, o controle de switch vai ocorrer via app,
onde é separado por LDAP de cada dev/qa e a api principal de MOCK vai interceptar todas as requisições através de um
proxy que vai consultar via Cache ou Redis se o endpoint chamado está em modo de gravação. Essa abordagem trás alguns
pontos negativos para a latência geral de todos os endpoints de mock, pois cada requisição deverá passar por um proxy e
posteriormente consultar se o endpoint está habilitado para gravação, porém não haverá necessidade de alteração manual
por parte do usuário.

## TODO feature de recording
[] Endpoint novo para salvar em cache LDAP + radix tree (start/stop recording)
[] Adaptar endpoint de mock para verificar se está em modo de gravação antes de buscar o mock
[] 