from flask import Flask, request, jsonify, session
from flask_session import Session
from functools import wraps
import os

app = Flask(__name__)
app.config['SECRET_KEY'] = 'your-secret-key'  # For signing session cookies
app.config['SESSION_TYPE'] = 'filesystem'  # Store sessions in filesystem
app.config['SESSION_FILE_DIR'] = './sessions'
Session(app)

# Dummy user database
users = {'test': 'pass123'}


# Login required decorator
def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'username' not in session:
            return jsonify({'error': 'Unauthorized'}), 401
        return f(*args, **kwargs)

    return decorated_function


@app.route('/login', methods=['POST'])
def login():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    if username in users and users[username] == password:
        session['username'] = username  # Store username in session
        return jsonify({'message': 'Login successful'})
    return jsonify({'error': 'Invalid credentials'}), 401


@app.route('/protected', methods=['GET'])
@login_required
def protected():
    return jsonify({'message': f'Hello, {session["username"]}! This is a protected endpoint.'})


if __name__ == '__main__':
    os.makedirs('./sessions', exist_ok=True)
    app.run(host='0.0.0.0', port=5000)