from openai import OpenAI
import os
from dotenv import load_dotenv

SYSTEM_MESSAGE = "You are a chatbot. You will have a conversation with a user. Be friendly and concise"

if __name__ == "__main__":
    load_dotenv()
    # Apunta al servidor local de ollama y ollama no requiere autenticacion local.
    #Modelo usado llama3.2
    URL = os.environ.get('OPENAI_BASE_URL')
    KEY = os.environ.get('OPENAI_KEY')
    MODEL = os.environ.get('MODEL')

    client = OpenAI(
        base_url=URL,
        api_key=KEY,
    )

    print(f"Chatting with {MODEL} model at {URL}\n")

    # Primer cambio fue mover el buffer de memoria fuera del loop
    conversation_history = [
        {'role': 'system', 'content': SYSTEM_MESSAGE}
    ]

    # Script funcionara hasta CTLR + C
    while True:
        message = input("> ")
        
        # Guardar el mensaje del usuario en el historial de la conversación
        conversation_history.append({'role': 'user', 'content': message})
        
        # Pasar el contexto previo al LLM
        response = client.chat.completions.create(
            model=MODEL,
            messages=conversation_history
        )
        
        
        reply = response.choices[0].message.content
        print(reply)
        
        # Primer cambio fue almacenar la respuesta del LLM en el historial.
        # Si no hacemos esto, el LLM escuchará el historial del usuario, pero olvidará sus propias respuestas.
        conversation_history.append({'role': 'assistant', 'content': reply})