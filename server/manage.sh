#!/bin/bash

SERVERS="Auth bedwars Bungee duels lobby lobbybw lobbyduels lobbypvp pvp"

case "${1:-}" in
  start)
    echo "Iniciando todos os servidores..."
    for sv in $SERVERS; do
      if screen -ls 2>/dev/null | grep -q "mc-$sv"; then
        echo "  [$sv] JA RODANDO"
      else
        screen -dmS "mc-$sv" bash -c "cd /home/$sv && bash start.sh"
        echo "  [$sv] INICIADO"
      fi
    done
    echo "Pronto!"
    ;;

  stop)
    echo "Parando todos os servidores..."
    for sv in $SERVERS; do
      if screen -ls 2>/dev/null | grep -q "mc-$sv"; then
        echo "  [$sv] Enviando stop..."
        screen -S "mc-$sv" -p 0 -X stuff "stop$(printf '\r')" 2>/dev/null

        count=0
        while screen -ls 2>/dev/null | grep -q "mc-$sv"; do
          sleep 2
          count=$((count + 2))

          if [ $count -ge 30 ]; then
            echo "  [$sv] Forcando parada..."
            screen -S "mc-$sv" -X quit 2>/dev/null
            break
          fi
        done

        echo "  [$sv] PARADO"

        case "$sv" in
          lobby|lobbybw|lobbyduels|lobbypvp)
            if [ -d "/home/$sv/world2" ]; then

              if [ -d "/home/$sv/world" ]; then
                echo "  [$sv] Restaurando world2 -> world..."

                rm -rf "/home/$sv/world/"*
                cp -a "/home/$sv/world2/." "/home/$sv/world/"

                echo "  [$sv] WORLD RESTAURADO"

              elif [ -d "/home/$sv/spawn" ]; then
                echo "  [$sv] Restaurando world2 -> spawn..."

                rm -rf "/home/$sv/spawn/"*
                cp -a "/home/$sv/world2/." "/home/$sv/spawn/"

                echo "  [$sv] SPAWN RESTAURADO"

              else
                echo "  [$sv] Nenhuma pasta 'world' ou 'spawn' encontrada."
              fi

            else
              echo "  [$sv] Pasta world2 nao encontrada."
            fi
            ;;
        esac

      else
        echo "  [$sv] NAO RODANDO"
      fi
    done

    echo "Todos parados."
    ;;

  status)
    echo "=== Servidores ==="
    for sv in $SERVERS; do
      if screen -ls 2>/dev/null | grep -q "mc-$sv"; then
        echo "  [$sv] RODANDO"
      else
        echo "  [$sv] PARADO"
      fi
    done
    ;;

  *)
    echo "Uso: $0 {start|stop|status}"
    exit 1
    ;;
esac