import type { RequestHandler } from './$types';

export const POST: RequestHandler = async ({ request }) => {
    const data = await request.json();

    const res = await fetch(
        "http://localhost:8080/api/services",
        {
            method: 'POST',
            body: JSON.stringify(data),
            headers: {
                "Content-type": "application/json"
            }
        }
    );

    if (!res.ok) {
        throw new Error("[SERVER]: An error has ocurred while trying to perform a HTTP call to /api/services");
    }

    const response = await res.json();

    return new Response(response.id);
};

