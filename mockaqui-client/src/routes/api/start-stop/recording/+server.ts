import { json } from '@sveltejs/kit';
import type { RequestHandler } from './$types';

export const PATCH: RequestHandler = async ({ request }) => {
    const ldap = request.headers.get("ldap");
    if (!ldap) {
        throw new Error("[SERVER]: Could not parse ldap header.");
    }

    const data = await request.json()

    try {
        const res = await fetch(
            "http://localhost:8080/api/start-stop/recording",
            {
                method: "PATCH",
                body: JSON.stringify(data),
                headers: {
                    "ldap": ldap,
                    "Content-type": "application/json"
                }
            }
        );

        if (!res.ok) {
            console.error("ERROR: unable to send start/stop request to mock api,");
            return json({ "success": false });
        }
    } catch (err) {
        console.error("ERROR: unable to send start/stop request to mock api, reason: ", err);
    }

    return json({
        "success": true
    });
};

