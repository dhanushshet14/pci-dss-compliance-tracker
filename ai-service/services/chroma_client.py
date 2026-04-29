import os
import logging
import chromadb
from chromadb.utils import embedding_functions

logger = logging.getLogger(__name__)

class ChromaClient:
    def __init__(self):
        self.client = chromadb.PersistentClient(path="./chroma_data")
        self.collection_name = "pci_dss_knowledge"
        self.embedding_fn = embedding_functions.DefaultEmbeddingFunction()
        logger.info("ChromaDB embedding function ready")
        self.collection = self.client.get_or_create_collection(
            name=self.collection_name,
            embedding_function=self.embedding_fn,
            metadata={"hnsw:space": "cosine"}
        )
        logger.info(f"ChromaDB collection ready: {self.collection_name}")

    def chunk_text(self, text: str, chunk_size: int = 500, overlap: int = 50):
        chunks = []
        start = 0
        while start < len(text):
            end = start + chunk_size
            chunk = text[start:end]
            if chunk.strip():
                chunks.append(chunk.strip())
            start = end - overlap
        return chunks

    def load_documents(self, docs_folder: str = "./docs"):
        total_chunks = 0
        if not os.path.exists(docs_folder):
            logger.warning(f"Docs folder not found: {docs_folder}")
            return 0

        files = [f for f in os.listdir(docs_folder) if f.endswith(".txt")]
        logger.info(f"Found {len(files)} documents to load")

        for filename in files:
            filepath = os.path.join(docs_folder, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                text = f.read()

            chunks = self.chunk_text(text)
            logger.info(f"File: {filename} — {len(chunks)} chunks created")

            for i, chunk in enumerate(chunks):
                doc_id = f"{filename}_{i}"
                try:
                    existing = self.collection.get(ids=[doc_id])
                    if existing["ids"]:
                        continue
                except Exception:
                    pass

                self.collection.add(
                    ids=[doc_id],
                    documents=[chunk],
                    metadatas=[{"source": filename, "chunk_index": i}]
                )
                total_chunks += 1

        logger.info(f"Total new chunks stored in ChromaDB: {total_chunks}")
        return total_chunks

    def query(self, question: str, n_results: int = 3):
        try:
            results = self.collection.query(
                query_texts=[question],
                n_results=n_results
            )
            return results
        except Exception as e:
            logger.error(f"ChromaDB query error: {str(e)}")
            return {"documents": [[]], "metadatas": [[]]}

    def get_doc_count(self):
        return self.collection.count()