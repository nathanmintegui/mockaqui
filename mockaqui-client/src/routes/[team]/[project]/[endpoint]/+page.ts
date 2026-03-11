import type { PageLoad } from './$types';

export const load: PageLoad = async ({ fetch }) => {
	const res = await fetch('http://localhost:8080/api/services');
	const projectsResponse = await res.json();

	const id = 1;
	const endpointsByCollection = await fetch(`http://localhost:8080/api/collections/${id}/endpoints`);
	const endpointsByCollectionResponse = await endpointsByCollection.json();

	const collections = [
		{
			id: 1,
			uri: '/bazz',
			endpoints: endpointsByCollectionResponse
		}
	];

	return {
		collections,
		projectsResponse
	};
};

