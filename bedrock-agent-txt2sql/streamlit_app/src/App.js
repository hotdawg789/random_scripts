import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [question, setQuestion] = useState('');
  const [response, setResponse] = useState('');
  const [traceData, setTraceData] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const result = await axios.post('http://localhost:5000/api/ask', {
        question,
        sessionId: 'MYSESSION'
      });
      setResponse(result.data.response);
      setTraceData(result.data.trace_data);
    } catch (error) {
      console.error('Error:', error);
      setResponse('An error occurred. Please try again.');
    }
  };

  return (
    <div className="App">
      <h1>Text2SQL Agent - Amazon Athena</h1>
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="Enter your query"
        />
        <button type="submit">Submit</button>
      </form>
      {response && (
        <div>
          <h2>Response:</h2>
          <p>{response}</p>
        </div>
      )}
      {traceData && (
        <div>
          <h2>Trace Data:</h2>
          <pre>{JSON.stringify(traceData, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default App;