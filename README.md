# Como correr
Realizar os próximos comandos no terminal a partir da pasta _src_.
Para compilar correr o comando: `./build.sh`
De forma a correr manualmento o server, mantendo-nos na diretoria atual correr: 
- `./runServer.sh`: Ligar o servidor.
- `./runClient.sh`: Ligar 1 cliente ao servidor.

# Correr testes
Compilar da mesma forma que anteriormente explicado.
Para correr os testes é preciso estar na diretotia dos scripts na pasta _test_, desta forma temos de fazer de utilizar os seguintes comandos (client ou server para entrar na diretoria do teste para um dos dois): `cd test/scripts/(client|server)`
De forma a correr ostestes é escolher um dos server na pasta _server_ e um dos workflows na pasta _client_ (escolher primeiro a pasta com o nº de clientes que se vai testar e depois correr os .sh).

# Problema de permissão
No caso dos ficheiros .sh darem erro é necessário dar permissão de execução aos mesmos: `chmod +x *.sh`

# Limpar a solução
Realizar o próximo comando no terminal a partir da pasta _src_.
Para limpar a solução utilizamos o comando: `./clean.sh`

# Nota de Projeto
19.2
